# Agent Observability 与评测方案（Langfuse）

> 状态：MVP 已落地：ReAct、Plan、Multi-Agent 观测与确定性评测 API 已接入；Runtime/微信入口沿用 Main 安装的全局 sink。
>
> 目标：在不改变 ReAct、Plan-and-Execute、Multi-Agent、MCP 和 TUI 行为的前提下，把一次 Agent turn 的运行过程组织成可追踪的 trace，并建立可重复的质量评测与版本回归闭环。

## 1. 为什么需要这层能力

当前项目已经有几类分散的诊断数据：

- `Agent` 日志、`LlmTraceLogger` 和请求上下文 token 估算
- `AgentBudget` 的真实 provider token、调用次数和退出原因
- `ToolRegistry` / `AuditLog` 的工具执行、HITL、策略拒绝和耗时
- `/export` 导出的 ReAct `conversationHistory`
- `SnapshotService` 的代码变更前后快照
- Runtime API 的 thread/event 时间线

这些数据足以排查单次问题，但还不能方便地回答：

- 一次任务经历了哪些 LLM、工具和子 Agent 节点？
- 哪个模型、Prompt 或工具版本导致了回归？
- 任务成功率、轨迹质量、成本和 P95 延迟是否改善？
- “最终回答正确但过程低效”和“过程合理但最终失败”分别有多少？

Langfuse 在本方案中作为观测和评测数据平台使用：负责 trace/span/generation 的组织、存储、查询、可视化和 Score 保存；任务成功判定和业务评测逻辑仍由 CodeCLI 自己实现。

## 2. 总体架构

```text
CLI / Runtime API / 微信入口
            |
            v
        Turn Trace
            |
  +---------+----------+----------+---------+
  |                    |          |         |
Prompt / Memory   LLM Generation  Tool     Snapshot
                                  / HITL
            |
            v
       Async TraceSink
        /          \
   Noop sink    Langfuse exporter
                     |
                     v
              自定义 Evaluator
       (确定性断言 + Judge + 人工反馈)
                     |
                     v
             Score 回写 Langfuse
             Dataset / Baseline / Delta
```

核心设计约束：

1. 业务代码只依赖 `TraceSink`，不直接依赖 Langfuse SDK。
2. 默认使用 `NoopTraceSink`，未配置 Langfuse 时零行为变化。
3. 上报采用异步、有界队列和批量发送，观测失败不能阻塞 Agent。
4. 并行工具和子 Agent 保留父子关系，trace 是树或 DAG，不是简单日志列表。

## 3. Trace 模型

### 3.1 根 Trace

一次用户提交（一个 turn）对应一个根 trace：

```text
trace_id
session_id
turn_id
mode: react | plan | team | runtime | wechat
project_hash
app_version
provider
model
started_at / ended_at
status: completed | failed | canceled | budget_exhausted
```

项目路径只记录稳定 hash 或脱敏标识，不上传绝对路径。

### 3.2 子 Observation

建议使用以下语义节点：

```text
turn
├── prompt.assemble
├── memory.retrieve
├── llm.generation
│   ├── tool.call: grep_code
│   ├── tool.call: read_file
│   └── tool.call: web_search
├── history.compact
├── hitl.decision
├── agent.child
└── final.answer
```

每个节点统一包含：

```text
observation_id
parent_observation_id
name / kind
started_at / ended_at
input_summary / output_summary
status
error
metadata
```

`llm.generation` 额外记录：

```text
provider
model
input_tokens
output_tokens
cached_input_tokens
time_to_first_token
tool_call_count
reasoning_chars
finish_reason
```

`tool.call` 额外记录：

```text
tool_name
sanitized_arguments
result_size
duration_ms
outcome: allow | deny | error
approver: hitl | policy | none | mention
mcp_server
parallel_batch_id
```

并行工具示例：

```text
generation-1
├── tool-grep_code
├── tool-read_file
└── tool-web_search
```

### 3.3 失败和取消

所有异常路径都必须结束当前 observation 并标记状态：

- LLM HTTP 错误：`llm.generation = error`
- 工具策略拒绝：`tool.call = deny`
- HITL 拒绝：`tool.call = deny, approver=hitl`
- `/cancel`：根 trace = `canceled`
- stagnation / hard iteration limit：根 trace = `budget_exhausted`

## 4. 项目埋点位置

| 位置 | 观测内容 |
|---|---|
| `Main.java` | CLI turn 根 trace；ReAct/Plan/Team 模式；提交、结束、取消 |
| `RuntimeApiServer.java` | API thread/turn 与 trace_id 关联；事件结果 |
| `WechatMessageLoop.java` | 微信消息与 Agent turn 关联，避免记录敏感媒体内容 |
| `Agent.java` | 每轮 generation、tool batch、最终答案、预算和错误 |
| `PlanExecuteAgent.java` | planner、task executor、reviewer、重试 |
| `AgentOrchestrator.java` / `SubAgent.java` | parent/child agent、worker、reviewer、并行批次 |
| `ToolRegistry.java` | 工具开始/结束、结果、耗时、allow/deny/error |
| `McpToolBridge` / `McpServerManager` | MCP server、工具调用和连接错误 |
| `MemoryManager.java` | 记忆检索数量、耗时、注入 token；不默认上传完整记忆内容 |
| `ConversationHistoryCompactor.java` | 压缩触发原因、压缩前后 token、保留轮数 |
| `AgentBudget.java` | provider usage、LLM 调用数、迭代数、退出原因 |
| `AuditLog.java` | 复用安全结果，不重复实现脱敏逻辑 |
| `SnapshotService.java` | pre/post turn、变更文件数、diff 摘要 |

## 5. 评测体系

### 5.1 确定性指标

优先使用测试、文件、diff 和策略结果进行判定：

```text
task_success
tests_passed
expected_file_hit
expected_text_hit
diff_match
expected_tool_path
tool_path_valid
policy_compliant
max_iterations_passed
```

代码任务的可信度顺序建议为：

```text
编译 / 测试结果 > 文件和 diff 断言 > 工具轨迹断言 > Judge > 人工印象
```

现有 `CodeSearchGoldenSetTest` 可以演进为第一批 Dataset，增加：

```json
{
  "id": "grep-main",
  "input": "grep_code 工具在哪里注册？",
  "expected_tools": ["grep_code", "read_file"],
  "expected_paths": ["src/main/java/com/codecli/tool/ToolRegistry.java"],
  "expected_text": "tools.put(\"grep_code\"",
  "max_iterations": 3
}
```

### 5.2 轨迹指标

从 trace 计算：

```text
tool_selection_accuracy
tool_order_validity
unnecessary_tool_call_rate
repeated_call_rate
average_iterations
stagnation_rate
plan_completion_rate
child_agent_success_rate
recovery_rate
```

这些指标用于解释失败原因，不应单独替代最终任务成功判定。

### 5.3 性能和成本指标

```text
TTFT
total_latency
P50 / P95 latency
input_tokens
output_tokens
cached_input_tokens
cache_hit_ratio
llm_call_count
tool_duration_ms
estimated_cost
```

优先使用 provider 返回的真实 usage；本地估算值应标记为 `estimated`。非流式调用没有 TTFT 时应记录为 unavailable，而不是伪造为 0。

### 5.4 Judge / 人工质量指标

Judge 适合处理难以用规则判断的质量维度：

```text
content_accuracy
answer_completeness
planning_quality
tool_selection_quality
tool_argument_quality
context_understanding
hallucination_free
explanation_clarity
```

每项建议 1–5 分，并要求 Judge 返回理由和证据：

```json
{
  "score": 4,
  "reason": "工具路径正确，但最终回答没有说明测试结果",
  "evidence": ["..."]
}
```

### 5.5 安全和可靠性指标

```text
policy_deny_rate
HITL_approval_rate
unsafe_allow_rate
MCP_error_rate
tool_error_rate
cancel_success_rate
compaction_success_rate
data_redaction_success_rate
```

出现严重安全违规时，Case 直接判定为 `FAIL`，不能被内容质量高分抵消。

## 6. Case 判定和报告

每个 Case 至少输出：

```text
status: PASS | WARN | FAIL
trace_id
task_success
objective_score
trajectory_score
quality_score
safety_score
latency
tokens
cost
failure_reason
```

建议规则：

```text
PASS：目标完成、确定性断言通过、无安全问题
WARN：目标完成，但轮数、成本、延迟或工具调用明显偏高
FAIL：目标未完成、测试失败、严重错误或安全违规
```

综合分可以采用如下初始权重：

```text
objective / task success   40%
trajectory quality         20%
answer quality             20%
safety                     10%
efficiency                 10%
```

安全项同时作为硬门槛。

## 7. Langfuse 接入边界

建议新增：

```text
src/main/java/com/codecli/observability/
├── TraceSink.java
├── TraceContext.java
├── TraceHandle.java
├── NoopTraceSink.java
├── LangfuseTraceSink.java
├── AsyncTraceExporter.java
├── TracePayloadSanitizer.java
└── TraceConfig.java

src/main/java/com/codecli/eval/
├── EvalCase.java
├── EvalRunner.java
├── Evaluator.java
├── DeterministicEvaluator.java
├── JudgeEvaluator.java
├── CompositeScore.java
└── EvalReport.java
```

Langfuse 的具体实现可以基于 OpenTelemetry 或 Langfuse ingestion API。业务层只调用 `TraceSink`，这样将来可以切换 Jaeger、Honeycomb 或自建后端。

建议配置：

```text
LANGFUSE_ENABLED=false
LANGFUSE_BASE_URL=https://cloud.langfuse.com
LANGFUSE_PUBLIC_KEY=...
LANGFUSE_SECRET_KEY=...
LANGFUSE_CAPTURE_MODE=metadata
LANGFUSE_SAMPLE_RATE=1.0
LANGFUSE_MAX_CONTENT_CHARS=8000
```

采集级别：

```text
OFF       不创建外部 observation
METADATA  只记录模型、耗时、token、工具名、状态和错误
FULL      额外记录经过截断和脱敏的文本内容
```

## 8. 隐私、可靠性和成本控制

- 复用 `AuditLog.sanitize()` 的 Bearer、token、key、password、authorization 脱敏规则。
- 默认不上传图片 base64，只记录 MIME、尺寸、payload 长度和 hash。
- tool result、网页内容、源码和 reasoning 必须有最大字符数和采样策略。
- 项目绝对路径、环境变量、浏览器登录态和 MCP header 不进入 trace。
- 导出队列必须有界；队列满、网络超时、服务端 5xx 时丢弃或降级，不影响主流程。
- Langfuse secret key 只从环境变量或用户配置读取，不进入日志和 Score。
- 生产环境优先评估自托管 Langfuse、数据保留期和跨境合规要求。

## 9. 分期实施

### Phase A：观测 MVP

- 加入 `TraceSink` 和 `NoopTraceSink`。
- 打通 `turn → generation → tool.call → final.answer`。
- 记录真实 token、耗时、provider/model、错误和工具状态。
- CLI、Runtime API、后台任务和微信入口统一 trace schema。
- 观测关闭时回归测试全部通过。

### Phase B：确定性评测

- Golden Set 转成 Dataset。
- 增加测试结果、文件/diff、工具路径和策略结果 evaluator。
- 生成 Case 级 `PASS/WARN/FAIL`。
- 将 Score 与 `trace_id` 回写 Langfuse。

### Phase C：Judge 和人工反馈

- 增加固定 Rubric 的 Judge evaluator。
- 保存分数、理由和证据。
- 支持人工修正和 badcase 标注。

### Phase D：版本回归

- 固定 Dataset、模型、Prompt 版本和工具版本。
- 支持 ReAct / Plan / Team、多模型批量运行。
- 输出 baseline Delta：成功率、P95、token、成本、工具准确率。

建议初始回归门槛：

```text
成功率下降 > 3%       FAIL
成本上升 > 20%         WARN
P95 延迟上升 > 30%     WARN
出现严重安全违规       FAIL
```

## 10. 验收标准

第一版完成的最低标准：

1. 一次普通 ReAct turn 在 Langfuse 中能看到根 trace、至少一次 generation、工具子 span 和最终状态。
2. 并行工具调用的 parent-child 关系正确。
3. provider token 和耗时与现有 `AgentBudget` / 日志一致。
4. 工具拒绝、错误、取消和预算耗尽能正确标记。
5. 未配置 Langfuse 时不影响现有 CLI、TUI、Runtime API 和测试。
6. Golden Set 至少能产生 `task_success`、`tool_path_valid`、`policy_compliant` 三项 Score。
7. Trace 中不出现 API key、Bearer token、图片 base64 和未截断的敏感工具参数。

## 11. 面试中的准确表述

> Langfuse 负责 Agent 运行轨迹的结构化采集、父子链路组织、可视化和 Score 存储；CodeCLI 自己负责定义任务成功标准，并通过测试、文件 diff、工具轨迹、安全审计和 Judge 计算评测分数。这样观测和评测解耦，同时可以对 ReAct、Plan、Team 以及不同模型做可重复的版本回归。

## 12. 当前实现与验收基线

- `com.codecli.observability` 已提供 Noop/Langfuse sink、异步有界队列、采样、脱敏和截断。
- ReAct、Plan、Multi-Agent 均记录根 trace、LLM generation、工具调用、`final.answer` 和 `task_success`。
- `com.codecli.eval` 提供 `EvalCase`、`EvalObservation`、`DeterministicEvaluator`、`EvalRunner` 与 JSON 报告输出；兼容现有 `golden-set.json`。
- 离线验收：`mvn -q -Dtest=TracePayloadSanitizerTest,LangfuseTraceSinkTest,DeterministicEvaluatorTest -DskipTests=false test`。
- 真实上报前启动本地 Langfuse，并通过环境变量设置 key；不要把真实 key 写入仓库。
