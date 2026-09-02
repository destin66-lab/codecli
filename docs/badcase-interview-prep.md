# Badcase 面试准备：开发过程中遇到的具体问题、分析与解决

> 本文档将简历四大技术点对应的真实开发问题整理为 10 个 badcase，每个都按
> **现象 → 排查 → 根因 → 解决 → 复盘** 的结构讲述，并锚定到具体代码位置，
> 便于面试时直接指认代码讲，站得住脚。
>
> 每个 badcase 末尾都新增 **"🎙 讲给面试听（可直接说的话）"** 段落——第一人称、
> 可直接照着念的话术，重点公关清楚 **怎样定位 → 怎样分析查询 → 怎样解决** 这条叙述链，
> 让面试官跟着你"破案"。
>
> 配套文档：`docs/interview-main-flow.md`（主流程）、`docs/resume-tech-points-interview.md`（技术点映射）

---

## 目录

1. [Badcase 1A｜压缩器"空转"——日志显示压缩成功，但发给 LLM 的 token 根本没降](#1a)
2. [Badcase 1B｜压缩切断 tool_call / tool_result 成对协议](#1b)
3. [Badcase 2A｜并行步骤的流式输出在终端上"糊"掉](#2a)
4. [Badcase 2B｜同一个 Worker 被并发占用，对话历史串台](#2b)
5. [Badcase 2C｜LLM 输出的 JSON 不稳定，解析失败崩掉或"坏结果被放行"](#2c)
6. [Badcase 3A｜退出时 close 卡 60 秒；server 握手慢拖慢启动](#3a)
7. [Badcase 3B｜工具 schema 太"脏"，模型看不懂、调用失败率高](#3b)
8. [Badcase 4A｜动态内容混进稳定前缀，KV cache 全 miss](#4a)
9. [Badcase 4B｜用户覆盖 prompt 导致"静默劣化"](#4b)
10. [Badcase 1C｜反复压缩后摘要"越压越淡"——历史信息逐级衰减](#1c)
11. [面试讲述方法：结构、节奏与避坑](#presentation)

---

## <a name="1a"></a>Badcase 1A｜压缩器"空转"——日志显示压缩成功，但发给 LLM 的 token 根本没降

**对应技术点**：双上下文压缩与长会话管理

**现象**

长对话跑到 30+ 轮之后，底层 provider 的请求体越来越大，最后直接报 `context_length_exceeded` 或费用飙升。查日志明明有压缩记录，短期记忆的 token 数也确实降了，但下一次请求的实际 input token 还是继续涨。

**排查**

先把"真正发给 LLM 的是什么"这条链路捋清楚。发现 `Agent` 主循环直接维护一份 `List<LlmClient.Message> conversationHistory`，每次调 LLM 传的就是它；而早期压缩器挂的是 `ConversationMemory`（以 `MemoryEntry` 为单位的短期记忆条目）。这两条数据流是**并行的**，并不互相同步——压缩了 Memory，`conversationHistory` 纹丝不动。

代码注释里甚至把这个坑写成了复盘记录（最有力的自证）：

```20:23:src/main/java/com/codecli/memory/ConversationHistoryCompactor.java
 * 第 3 期 Memory 设计假设"LLM 调用从 shortTermMemory 重建消息"，但实际 Agent 直接维护
 * conversationHistory，与 shortTermMemory 并行。两个度量错位导致旧版压缩从未真正缩短
 * 即将发给 LLM 的 token——本类是在 Agent.run 主循环里"调 LLM 前评估并压缩"的补丁。
```

**根因**

设计假设与实现错位：最初假设"LLM 调用从 shortTermMemory 重建消息列表"，但实现上 Agent 直接维护 history，Memory 只是旁路的检索层。**压缩挂错了层**，真正决定输入 token 的那份数据没人管。

**解决**

- 新增 `ConversationHistoryCompactor`，挂到 `Agent.run` 主循环"调 LLM 之前"，对真正要发的 history 做估算和压缩；
- 先用 `TokenBudget.estimateMessagesTokens` 估算，`compactIfNeeded` 未达阈值直接返回，避免无谓的摘要开销；
- 提供 `compactNow`（跳过阈值、强制压缩、只保留 1 轮），给手动触发用；
- 摘要 LLM 调用失败或返回空时 `log.warn` 并跳过本次压缩（`return false`），宁可下次再压，绝不让一次失败把会话搞崩。

**复盘/延伸**

这个 bug 教会我一件事：**监控要对准真正的瓶颈**。判断压缩有没有生效，应该看 provider usage 里返回的 input token，而不是只看自己日志里的估算值。被追问"怎么验证压缩有效"时，回答：`AgentBudget` 累计 `totalInputTokens`，对比压缩前后同一任务的 token 曲线。

### 🎙 讲给面试听（可直接说的话）

> 这个 bug 是我在跑长对话压测时撞出来的：请求体越滚越大，最后直接 `context_length_exceeded`。但奇怪的是，我查自己日志明明有"压缩成功"的记录、token 数也在降——这让我第一反应是"日志和真实发送对不上"。于是顺着"每次调 LLM 前到底传的是什么"这条链路追，发现 Agent 是直接维护一份 history 列表发请求的，而我的压缩器挂在短期记忆上——两条数据流是并行的、根本不同步。定位到根因是"压错了层"，真正决定输入 token 的那份数据没人管。解决是新增针对 history 的压缩器，挂到"调 LLM 之前"评估、达到阈值才压，还分了自动/手动两个入口，摘要失败就跳过本次。这个坑让我学到：**判断一个优化有没有生效，要看真正影响结果的那份数据，而不是看自己日志里的估算值**。

---

## <a name="1b"></a>Badcase 1B｜压缩切断 tool_call / tool_result 成对协议

**对应技术点**：双上下文压缩与长会话管理

**现象**

压缩后出现一种很隐蔽的劣化：模型开始"幻觉工具调用失败"，比如反复说"我看到工具调用但没有返回结果"，或者把某次文件编辑的结果记错，后续步骤基于错误结果继续执行。

**排查**

把压缩前后的完整 history dump 出来逐条对比，发现分割点落进了一条 `assistant(tool_calls=[...])` 消息的中间——tool_call 被摘要掉了，对应的 tool_result 却留在尾部。在 ReAct 循环里，一轮 user 之后可以跟好几对 `assistant(tool_call) → tool(tool_result)`，**如果在中间切，必然破坏成对协议**。模型看到孤立的 tool_result、或者只有 tool_call 没有 result，行为就开始漂移。

**根因**

早期版本按"消息条数"或"固定 token 位置"切分，没有约束切分点的结构合法性。

**解决**

把"切分点必须落在 user message 边界"固化为硬约束（代码注释已写明）：

```27:30:src/main/java/com/codecli/memory/ConversationHistoryCompactor.java
 * 4. 重建：[system] + [user("[已压缩的历史对话摘要]\n" + summary)] +
 *         [assistant("好的，已了解上下文。请继续。")] + [尾部保留消息]
 *
 * 关键约束：分割点必然落在 user message 边界，避免切断 tool_call / tool_result 的成对协议。
```

- 扫描所有 user 消息的索引，`splitIdx = userIndices.get(size - retainRounds)`，**被压缩区间永远以 user 开头、在完整消息边界结束**，tool_call/tool_result 要么成对被压、要么成对保留；
- 重建时插入 `[user(摘要)] + [assistant("好的，我已了解之前的上下文，请继续。")]`，保持对话轮次对齐，模型不会觉得上下文"跳变"；
- 为这个不变量写了专门测试（`ConversationHistoryCompactorTest`）。

**复盘/延伸**

核心是：**只追求"token 变少"这个量化指标是不够的，还要满足"消息结构合法"这个协议约束**。把"切分点落在 user 边界"当成一个不变量来测，而不是靠感觉。被追问"摘要丢了信息怎么办"：`retainRecentRounds=3` 保留最近 3 轮完整消息 + 摘要 prompt 强制保留四类关键信息（用户诉求 / 已完成操作 / 共识结论 / 未解决问题）。

### 🎙 讲给面试听（可直接说的话）

> 这个是我在压缩上线后观察到的隐蔽劣化：模型开始莫名其妙地"幻觉工具调用失败"，反复说看到一个调用但没有结果，或者把某次文件编辑记错、后续基于错误结果继续。我把压缩前后的完整消息 dump 出来逐条对比，发现分割点硬生生落在一条 assistant 工具调用消息的中间——前半段被摘要掉了，对应的 tool_result 却留在尾部，成对的 tool_call/tool_result 协议被拦腰斩断。根因是早期按"消息条数"切，完全没管消息结构。解决是把它改成一个**硬约束**：先扫描所有 user 消息的索引，分割点必须落在 user message 边界，这样 tool_call/tool_result 要么成对被压缩、要么成对保留；重建时再补一条 assistant 确认消息对齐轮次。这个 case 让我明白：优化不能只看 token 变少这个量化指标，还要保证消息结构合法，所以我把它固化成专门测试的不变量来防回归。

---

## <a name="1c"></a>Badcase 1C｜反复压缩后摘要"越压越淡"——历史信息逐级衰减

**对应技术点**：双上下文压缩与长会话管理

**现象**

长会话自动压缩跑到第 3、4 次之后，模型开始"选择性失忆"：早期用户明确说过的关键约定（比如"项目用 Java 17"）越来越模糊，甚至要重复提醒。把几次压缩后的 SUMMARY 条目翻出来逐版对比，发现第 N 次生成的摘要明显比第 N-1 次更笼统——**每次压缩都丢一点细节，像照片反复复印**。

**排查**

看 `ContextCompressor.compress()` 的完整流程，问题出在 `memory.clear()` 这一步：它把**上一次生成的 SUMMARY 条目也当成普通旧消息**一起清掉，然后重新走 map-reduce。第二次压缩时，喂给模型的是一段"浓缩过的旧摘要 + 少量新消息"——模型面对抽象过一次的文字，很难在重压时还原里面已经丢掉的细节，于是每压一次，信息就衰减一层。

**根因**

压缩策略是"从零归纳"而不是"增量更新"：每次压缩都把旧摘要当成普通历史重新归纳一遍，旧摘要里的信息在"第二次抽象"中再次损失。**反复压缩 = 反复二次抽象 = 逐级衰减**，与单次压缩的质量无关。

**解决**

把"上一版摘要"从普通历史中摘出来，单独做增量合并：

1. 压缩前先扫描 `oldEntries`，把 `SUMMARY` 类型条目单独取出为 `previousSummary`，**不进入 map 摘要**（避免被当作普通历史重压）：

```137:147:src/main/java/com/codecli/memory/ContextCompressor.java
        String previousSummary = null;
        List<MemoryEntry> extractable = new ArrayList<>(oldEntries.size());
        for (MemoryEntry entry : oldEntries) {
            if (entry.getType() == MemoryEntry.MemoryType.SUMMARY) {
                previousSummary = entry.getContent();
            } else {
                extractable.add(entry);
            }
        }
        oldEntries = extractable;
```

2. 新增 `UPDATE_PROMPT` 更新式摘要模板，显式要求"保留旧摘要全部信息 + 只补充新进展"：

```45:56:src/main/java/com/codecli/memory/ContextCompressor.java
    private static final String UPDATE_PROMPT = """
            下面是上一轮生成的对话摘要。请把"本轮新摘要"合并进去，规则：
            - 保留上一轮摘要里的全部关键信息，不要丢弃或改写
            - 补充本轮新摘要里的新进展、新决策、新上下文
            - 保留精确的文件路径、函数名、错误信息
            - 输出合并后的完整摘要，中文，控制在400字以内
            ...
```

3. 生成本轮摘要后，若有 `previousSummary` 就走 `updatePhase()` 合并；LLM 调用失败时降级为"旧摘要 + 新摘要"拼接，至少不丢新内容：

```300:314:src/main/java/com/codecli/memory/ContextCompressor.java
    private String updatePhase(String newSummary, String previousSummary) {
        // ...
            return updated == null || updated.isBlank() ? previousSummary + "\n" + newSummary : updated;
        } catch (IOException e) {
            System.err.println("⚠️ 摘要更新失败: " + e.getMessage());
            return previousSummary + "\n" + newSummary;
        }
    }
```

4. 为"旧摘要不再进入 map 重压 + 更新阶段被调用"两个不变量写专门测试（`ContextCompressorTest.secondCompressionMergesPreviousSummaryInsteadOfResummarizing`），防回归。

**复盘/延伸**

压缩系统的"丢失"要拆成两个维度分开防：`extractFacts` 防的是**宽度**丢失——稳定事实被压没、且没进长期记忆（跨会话找不回）；增量摘要防的是**深度**丢失——摘要还在，但被反复重压逐级变淡。核心一句话：**摘要应该"站在上一版的肩膀上更新"，而不是每次从零重压**。被追问"压缩丢信息怎么办"，可以给出这张二维防线：宽度交给长期记忆沉淀，深度交给增量合并。

### 🎙 讲给面试听（可直接说的话）

> 长会话自动压缩跑到第三四次后，模型开始"选择性失忆"：早期用户明确说过的约定越来越模糊，甚至要我重复提醒。我把几次压缩后的摘要条目翻出来逐版对比，发现第 N 次生成的摘要明显比第 N-1 次更笼统——每次都丢一点细节，像照片反复复印。追到压缩代码，根因是 `memory.clear()` 把上一次生成的摘要也当成普通旧消息一起清掉、重新走 map-reduce：模型面对抽象过一次的文字，很难在重压时还原已经丢掉的细节，所以每压一次衰减一层。解决是把上一版摘要从普通历史里摘出来单独处理：先扫描出 `SUMMARY` 条目作为 `previousSummary`、不进 map 重压；再新增一个"更新式"摘要 prompt——保留旧摘要全部信息、只补充新进展；LLM 失败就降级为拼接，至少不丢新的。配套测试把"旧摘要不再被重压、更新阶段被调用"锁成不变量。这件事让我把压缩的丢失拆成两个维度：`extractFacts` 防"宽度"（事实没进长期记忆就找不回），增量摘要防"深度"（反复压缩逐级变淡）——一个管存不存得住，一个管记得清不清楚。

---

## <a name="2a"></a>Badcase 2A｜并行步骤的流式输出在终端上"糊"掉了

**对应技术点**：多 Agent 协作与并行调度

**现象**

第一批可并行执行的步骤同时跑起来后，终端输出完全花掉：worker-1 和 worker-2 的文本逐段交错，用户根本看不出哪个步骤在干什么。

**排查**

并行路径里每个 worker 是独立线程，流式回调（LLM 的 `onDelta`）又是从各自的网络线程回调进来的，多线程无锁写同一个 `System.out` → 字符级竞争。用多步骤任务稳定复现后，确认是"输出资源的并发竞争"，不是数据本身的问题。

**根因**

并行只隔离了"计算"，没隔离"输出"。多个生产者线程直写同一个消费者资源（终端流），没有缓冲也没有排序。

**解决**

设计决策（代码注释已写明）：

```31:38:src/main/java/com/codecli/agent/AgentOrchestrator.java
 * 并行策略：
 * - 同一依赖批次内部 **并行** 执行（最多 Worker 池大小并发，默认 2）
 * - 每个并行步骤使用独立的 PrintStream 缓冲流式输出，批次结束后按 step_id 顺序 flush 到 stdout，
 *   避免多线程写同一个终端流造成交错，同时仍让用户看到结构化的执行过程
 * - 单步批次仍走直连流式路径，保持"实时打字"的观感
 * - Worker 通过 {@link java.util.concurrent.BlockingQueue} 池化分配，确保同一 Worker 不会被两个步骤并发占用
 * - Reviewer 在并行路径中按步骤即时创建独立实例，避免对话历史竞争
```

- 每个并行步骤一个独立的 `ByteArrayOutputStream` + `PrintStream`，所有流式输出先写本地缓冲，**不直接碰 stdout**；
- 批次内所有任务完成后按 `step_id` 顺序 flush 到 stdout，顺序稳定；
- 特判：批次只有一个可执行步骤时，仍走直连流式路径，保留实时打字观感。

**复盘/延伸**

trade-off：**全缓冲保有序、牺牲实时性；单步直连保实时、牺牲有序性**。按"批次大小"分流兼顾两者。被问"为什么不全用缓冲"就用这个回答。

### 🎙 讲给面试听（可直接说的话）

> 第一批可并行步骤一跑起来，终端就"花"了：worker-1 和 worker-2 的文字逐段交错，用户根本看不出哪个步骤在干什么。我先怀疑是数据问题，用多步骤任务稳定复现后发现其实是**输出**问题——每个 worker 是独立线程，流式回调（LLM 的 onDelta）又是从各自网络线程回调进来的，多线程无锁写同一个 System.out，就是字符级竞争。根因是并行只隔离了"计算"、没隔离"输出"。解决是每个并行步骤写进自己独立的缓冲流，跑完整个批次再按 step_id 顺序统一 flush 到标准输出，顺序稳定；同时留了个特判——批次只有单步时仍走直连流式，保留实时打字的体验。这里的关键 trade-off 是：全缓冲保有序、牺牲实时性；单步直连保实时、牺牲有序性，所以我按批次大小分流。

---

## <a name="2b"></a>Badcase 2B｜同一个 Worker 被并发占用，对话历史串台

**对应技术点**：多 Agent 协作与并行调度

**现象**

两个并行步骤执行完后，步骤 A 的输出里混进了步骤 B 的内容；或者步骤 A 的结果明明是好的，Reviewer 却说"这与任务无关"。

**排查**

看并行提交逻辑，早期版本是直接 `workers.get(0)`、`workers.get(1)` 按索引分配。当批次里的步骤数超过 worker 数、或两个任务几乎同时提交时，两个线程可能拿到**同一个 `SubAgent` 实例**。每个 SubAgent 有自己的一份 `conversationHistory`，两个线程并发 append 消息并发送——历史串了，模型上下文里一半是任务 A、一半是任务 B。

**根因**

共享可变状态（SubAgent 的 history）被并发访问，没有互斥，也没有"一个 worker 同时只服务一个步骤"的约束。

**解决**

- 用 `BlockingQueue<SubAgent>` 做 Worker Pool：每个步骤 `workerPool.take()` 取 worker（没有空闲就阻塞等），用完 `workerPool.offer()` 归还；
- 并行步骤各自 `new` 一个独立 Reviewer（`reviewer-{stepId}`），避免 review 历史竞争；
- 步骤结束 `worker.clearHistory()`，防止上一个任务的上下文泄漏到下一个步骤。

```405:409:src/main/java/com/codecli/agent/AgentOrchestrator.java
     * 每个步骤获取一个 Worker（池化，避免同一 Worker 被两个步骤并发占用），同时创建独立的 Reviewer 实例，
     * 流式输出写入步骤本地的 ByteArrayOutputStream；所有任务完成后按 step_id 顺序将缓冲区 flush 到 stdout。
     */
    private void runBatchParallel(List<ExecutionStep> batch, List<ExecutionStep> steps,
```

**复盘/延伸**

"池化 + 取用归还"是处理"一组有限共享资源被并发任务使用"的通用模式。解释为什么并行度默认 2：太高竞争 LLM 的 API 速率限制，太低吃不到并行红利，2 是实测的平衡点。

### 🎙 讲给面试听（可直接说的话）

> 这个现象很怪：两个并行步骤跑完后，步骤 A 的输出里混进了步骤 B 的内容，Reviewer 还老说"这与任务无关"。我查并行提交逻辑，早期版本是直接按索引取 worker 的——当批次里的步骤数超过 worker 数、或两个任务几乎同时提交时，两个线程就拿到了**同一个** SubAgent 实例。而每个 SubAgent 有自己的一份对话历史，两个线程并发往里面 append 消息并发送，历史就串了，模型上下文里一半是任务 A、一半是任务 B。根因是**共享可变状态被并发访问**。解决是做成 `BlockingQueue` 的 Worker Pool：`take()` 拿 worker、用完 `offer()` 归还，保证一个 worker 同一时刻只服务一个步骤；并行步骤还各自 new 一个独立 Reviewer 避免 review 历史竞争；步骤结束清空 history 防泄漏。这是"一组有限共享资源被并发任务使用"时的通用解法。

---

## <a name="2c"></a>Badcase 2C｜LLM 输出的 JSON 不稳定，解析失败崩掉或"坏结果被放行"

**对应技术点**：多 Agent 协作与并行调度

**现象**

Planner 偶尔输出被 ```json 代码块包裹、字段名漂移（`steps` vs `tasks`）、id 是 `"1"` 而不是 `"step_1"`，甚至依赖了后面才定义的步骤；Reviewer 偶尔不输出 JSON，直接写一句"通过"或一段话。更糟的是早期版本解析失败默认放行——审查形同虚设，有问题的结果直接进最终汇总。

**排查**

收集多份 Planner / Reviewer 的真实输出做格式统计，发现 LLM 输出的格式稳定性远低于预期。结论：不是单个模型的问题，是"协议解析"这层必须自己扛住不确定性。

**解决**

三处防御性设计：

1. **`parsePlan` 容错四件套**：剥 ```json fence；兼容 `steps`/`tasks` 两个字段；重编号 id（`step_1...`）并维护映射解决依赖前向引用；解析失败返回空列表，由上层显式提示"无法解析执行计划"。
2. **`parseReviewApproval` 保守策略**——整个编排的安全底线：

```301:306:src/main/java/com/codecli/agent/AgentOrchestrator.java
    /**
     * 解析检查者的审批结果
     *
     * 解析失败时采取保守策略：默认判为"不通过"，避免在审查者异常输出时让问题结果直接放行。
     */
    boolean parseReviewApproval(String reviewContent) {
```

解析失败默认"不通过"，再降级为关键词匹配——只有"同时不含否定词且含肯定词"才判通过，其余全部不通过。
3. **环检测 + 重试上限**：`computeExecutionOrder()` 用 DFS 染色检测循环依赖，有环返回 false 并抛 `IOException("计划中存在循环依赖")`；重试上限 2 次，超限"保留当前结果"而不是无限循环。

**复盘/延伸**

核心设计哲学一句话：**对 LLM 的不可靠输出做 fail-safe 处理——拿不准就按保守的来，而不是按乐观的来**。"解析不了 → 默认不通过"比"解析不了 → 默认放行"安全得多。这是面试官最喜欢的"安全默认"表述。

### 🎙 讲给面试听（可直接说的话）

> LLM 输出的 JSON 稳定性远低于预期：Planner 有时带 markdown 代码块、字段名漂移（steps/tasks）、id 不统一，甚至还依赖后面才定义的步骤；Reviewer 干脆不输出 JSON，直接写一句"通过"或一段话。早期版本解析失败默认放行，审查等于形同虚设，有问题的结果直接进最终汇总。我收集了多份真实输出做格式统计，确认这不是单个模型的问题，而是"协议解析这一层必须自己扛住不确定性"。解决做了三层：解析计划时剥代码块、兼容多个字段、重编号 id 并用映射处理前向依赖，解析失败显式报"无法解析执行计划"；Reviewer 结果是**保守策略**——解析不了默认不通过，再降级成关键词匹配兜底，绝不放行可疑结果；再加环检测和重试上限防死循环。核心哲学一句话：对 LLM 的不可靠输出做 fail-safe，拿不准就按保守的来，而不是按乐观的放行。

---

## <a name="3a"></a>Badcase 3A｜退出时 close 卡 60 秒；server 握手慢拖慢启动

**对应技术点**：手写 MCP 协议与工具生态集成

**现象**

程序退出时在"关闭 MCP server"这一步卡了很久（最长 60 秒）；另外某个 server 启动慢（`npx` 首次拉包、远程 HTTP 握手慢）时，`initialize` 握手把整个 CLI 启动拖住。

**排查**

看 `McpClient.close()` 路径，早期版本会先发 `shutdown` notification 等待 server 优雅关闭。但 server 是不可控的第三方进程——它卡死、或消息队列堵塞时，这个等待就是 60 秒。代码注释里完整记录了这次的决策：

```264:268:src/main/java/com/codecli/mcp/McpClient.java
    @Override
    public void close() {
        // 直接走 transport-level 关闭信号：stdio 通过 stdin EOF + 进程销毁；HTTP 通过 DELETE session。
        // 之前会先发 shutdown notification，但当 server 卡死 / 队列堵塞时这条通知会让 close 阻塞 60 秒。
        // 移除后退出更快、行为更可预期；shutdown 语义改由 transport 层承担。
        rpc.close();
    }
```

**根因**

把"发 shutdown 并等待响应"放在 client 层，而 client 层对不可控的 server 没有强保证，等待窗口设得过大。

**解决**

把 shutdown 语义下沉到 transport 层，close 不再等 server 的 shutdown 响应，改用**分级退出的时间预算**：

- stdio：先关 stdin（子进程读到 EOF 触发优雅退出）→ 等 1 秒 → `destroy()` → 再等 2 秒 → `destroyForcibly()`；
- HTTP：直接 `DELETE session`。

另外每个 server 独立线程启动，单个 server 失败只标 ERROR、不阻塞其他 server；初始化超时做成可配置（`codecli.mcp.initialize.timeout.seconds` / 环境变量）。

**复盘/延伸**

进程生命周期管理里很经典的坑：**优雅关闭必须设时间预算，不能无限等一个不可控的子进程**。可以顺带讲 stdio 的 stdout/stderr 两个 daemon 线程 + 200 行环形缓冲（`stderrRing`），方便排查 server 自身报错——"可观测性"的加分细节。

### 🎙 讲给面试听（可直接说的话）

> 我第一次发现是程序退出时会卡很久，实测最长 60 秒。看 `close()` 路径，早期版本会先发 `shutdown` 通知、等 server 优雅关闭；但 server 是不可控的第三方进程——它卡死或消息队列堵塞时，这个等待就是 60 秒。根因是"关关闭语义放在了 client 层"，而对不可控进程没有超时的强保证。解决是把 shutdown 下沉到 transport 层，退出改成**分级时间预算**：先关 stdin 触发子进程优雅退出，等 1 秒不行就 destroy，再等 2 秒不行就强杀；HTTP 走 DELETE session。顺带修了启动：每个 server 独立线程启动，单个失败只标 ERROR、不阻塞其他 server，初始化超时也做成可配置。这里我学到：**优雅关闭必须设时间预算，不能无限等一个不可控的子进程**。旁边还能补一句可观测性——stdio 有 stdout/stderr 两个 daemon 线程，stderr 存 200 行环形缓冲，方便 `/mcp logs` 排查 server 自己的报错。

---

## <a name="3b"></a>Badcase 3B｜工具 schema 太"脏"，模型看不懂、调用失败率高

**对应技术点**：手写 MCP 协议与工具生态集成

**现象**

某个 server 注册的工具，模型经常传错参数或干脆不调用。把发给 LLM 的 tool definitions 打出来，发现 schema 里带 `$ref`、`anyOf`、几千字符的超长 description——token 很大但信息密度很低，部分模型直接报"无法解析参数 schema"。

**根因**

MCP 协议只约定 `inputSchema` 是 JSON Schema，但各家 server 的 schema 风格差异巨大，原始 schema 不适配 LLM function calling 的约定（要求扁平、可直读、参数明确）。

**解决**

加 `McpSchemaSanitizer.sanitize()` 在注册前统一清洗：删 `$schema` / `$id` / `$ref`（防止模型看到无法解析的引用）；把 `anyOf` / `oneOf` 拍平成 `type: object` + 人读的 description；description 截断到 1000 字符；最后强制保证 schema 有 `type` 和 `properties`，否则 fallback 成空对象 schema。注册进 `ToolRegistry` 的每个工具都是"干净、扁平、可直读"的。

**复盘/延伸**

这是"协议标准 vs 真实生态"适配的经典场景，也是"为什么手写而不是用官方 SDK"的一个论据：官方 SDK 不会替你做这种面向 LLM 的 schema 归一化，你自己写才能精准控制。被问"你手写 MCP 值不值"时，这就是现成答案。

### 🎙 讲给面试听（可直接说的话）

> 接某个 MCP server 后我发现：模型经常传错参数，甚至干脆不调用那个工具。我把发给 LLM 的 tool definitions 打出来，发现 schema 里带 `$ref`、`anyOf`、几千字符的超长 description——token 占得不少但信息密度很低，部分模型直接报"无法解析参数 schema"。根因是各家 server 的 `inputSchema` 风格差异巨大，不适配 LLM 对 function calling 的约定。解决是加一个 `McpSchemaSanitizer` 在注册前统一清洗：删掉 `$ref` 这类引用、把 `anyOf`/`oneOf` 拍平成一句人话、截断超长 description、兜底保证 schema 一定有 type 和 properties，这样每个工具对模型都是扁平、可直读的。这里顺手能回答"为什么手写而不直接用官方 SDK"——官方 SDK 不会替你做这种面向 LLM 的 schema 归一化，你自己写才能精准控制每个细节。

---

## <a name="4a"></a>Badcase 4A｜动态内容混进稳定前缀，KV cache 全 miss

**对应技术点**：Prompt 分层架构与 KV Cache 优化

**现象**

多轮对话里，provider usage 返回的 `prompt_cache_hit_tokens` 几乎为 0，每一轮都把整个 system prompt 重算一遍。甚至出现一个反直觉的现象：第 3 轮的输入成本比第 1 轮还高——说明完全没吃到缓存。

**排查**

看旧版 system prompt 的组装代码，它是"身份说明 → 记忆检索结果 → 工具策略 → …"的顺序，把**每轮都可能变化的记忆检索结果**插在很靠前的位置。KV cache 是**严格前缀匹配**的：只要前缀里第 N 个 token 变了，第 N 个之后的所有 KV 都要重算。记忆结果一变 → 整个前缀失效 → 全部重算 → 命中率 0%。

**根因**

组装顺序没考虑 cache 的"前缀敏感性"，把动态内容放在了稳定内容前面，等于给每轮请求都换了个新前缀。

**解决**

重构为分层 Markdown + 固定组装顺序，"稳定在前、动态在后"，现在的 `assemble()` 顺序：

```20:46:src/main/java/com/codecli/prompt/PromptAssembler.java
    public String assemble(PromptMode mode, PromptContext context) {
        ...
        append(prompt, base);
        append(prompt, repository.loadRequired("personalities/calm.md"));
        append(prompt, applyVariables(repository.loadRequired(mode.resourcePath()), ctx));
        append(prompt, repository.loadRequired("approvals/" + approvalMode(ctx) + ".md"));
        append(prompt, runtimeContext());
        append(prompt, dynamicSection("Project Context", ctx.projectMemoryContext(), ctx.memoryContext(),
                ctx.externalContext()));
        append(prompt, dynamicSection("Skills", ctx.skillIndex()));
        append(prompt, repository.loadRequired("context/context-management.md"));
        append(prompt, repository.loadRequired("handoff.md"));
```

- 稳定段（`base` → `personality` → `mode` → `approval`，占 system prompt 大头）前置，整个会话不变；
- 动态段（记忆 / PAI.md / MCP 索引 / Skill 索引）全部挪到中后部，每轮只重算尾部；
- 度量上接 provider usage 的 `prompt_cache_hit_tokens / (hit + miss)`，改造后首轮之后稳定在 90%+。

**复盘/延伸**

简历里"0% → 90%"这个数字，面试时**必须主动讲清度量口径**：统计的是 provider usage API 返回的缓存命中 token 占比（如 DeepSeek 的 `prompt_cache_hit_tokens`），不是自己估的。口径讲得清，数字才可信。

### 🎙 讲给面试听（可直接说的话）

> 多轮对话里我发现 provider usage 返回的 `prompt_cache_hit_tokens` 几乎为 0，甚至第 3 轮的输入成本比第 1 轮还高——说明完全没吃到缓存，每轮都把整个 system prompt 重算一遍。看旧版组装代码，它把每轮都会变的记忆检索结果插在很靠前的位置；而 KV cache 是**严格前缀匹配**的——前缀里第 N 个 token 一变，第 N 个之后的所有 KV 都要重算，所以整个前缀失效。根因是组装顺序没考虑"前缀敏感性"，等于每轮都换了个新前缀。解决是重构成分层 Markdown 加固定组装顺序：稳定段（base/性格/模式/审批）前置、整个会话不变，动态的记忆/skill 挪到中后部，每轮只重算尾部，命中率就上来了。度量上我接 provider usage 的 hit/miss 字段算占比。这里我特意提醒自己：讲数字必须讲清统计口径，说的是缓存命中 token 占比，不是自己估的。

---

## <a name="4b"></a>Badcase 4B｜用户覆盖 prompt 导致"静默劣化"

**对应技术点**：Prompt 分层架构与 KV Cache 优化

**现象**

用户在 `~/.codecli/prompts/` 或项目里放了自定义 `base.md`，结果模型开始输出英文、不遵守工具策略，而且"昨天还好好的"。

**排查**

`PromptRepository` 是整文件替换式覆盖，覆盖文件存在就整体替换内置文件。如果覆盖文件把关键的 `## Language` 段删了或改坏了，组装出来的 prompt 就"带伤上岗"且不报任何错——静默劣化，等模型行为漂移了你才回头找。

**解决**

组装后加 `validateLanguageSection` 硬校验：`base.md` 和最终组装结果必须包含 `## Language` section，缺失直接抛 `IllegalStateException`——**fail fast，把问题暴露在启动时，而不是等行为漂移**。

```27:46:src/main/java/com/codecli/prompt/PromptAssembler.java
        validateLanguageSection(base, "base.md");
        ...
        validateLanguageSection(assembled, "assembled prompt");
```

**复盘/延伸**

"覆盖要可审计"的思路：宁可启动失败，不可带病运行。这是工程上"fail fast 优于静默劣化"的典型体现。

### 🎙 讲给面试听（可直接说的话）

> 用户自定义了 `base.md`（或项目里放了 `.codecli/prompts/base.md`）之后，模型开始输出英文、不遵守工具策略，而且"昨天还好好的"，特别难查。我追下来发现 prompt 是"整文件替换式"覆盖——只要覆盖文件存在，就整体替换内置文件；而那份覆盖文件把关键的 `## Language` 段删了，组装出来的 prompt 就带伤上岗、但完全不报错，属于静默劣化，等模型行为漂移了才回头找线索。解决是组装后加一层**硬校验** `validateLanguageSection`：base.md 和最终组装结果必须包含 `## Language`，缺失直接抛异常 fail-fast，把问题暴露在启动时而不是行为漂移之后。这是一个典型的"宁可启动失败，也不带病运行"的例子。

---

## <a name="presentation"></a>面试讲述方法：结构、节奏与避坑

### 1. 叙事结构（四段式，一个问题控制在 2-3 分钟）

> **背景**（一句话说明发生了什么、当时在做什么）→ **现象**（客观观察，最好带数字/日志）→ **排查**（我的分析路径：先看哪、怎么定位）→ **根因**（一句话说透）→ **解决**（做了哪几步）→ **复盘**（学到了什么、怎么防止再犯）

不要先讲答案，要让面试官跟着你"破案"。分析路径（排查过程）比最终方案更值钱，因为它展示的是**方法论**，不是记忆。

### 2. 主动抛钩子：挑 2 个最出彩的 badcase 主动讲

> "这个项目里印象最深的是三个翻车现场：第一个是压缩器挂错了层、压了半天 token 没降；第二个是并行执行输出在终端上糊掉；第三个是退出时关 MCP server 卡 60 秒。我各讲一下怎么定位和解决的。"

推荐优先级（按"代码里有实锤 + 故事性强"排）：

| 优先级 | badcase | 为什么好讲 |
|---|---|---|
| ★★★ | 1A 压缩器空转 | 代码注释里直接有"度量错位"的复盘记录，最真实 |
| ★★★ | 3A close 卡 60 秒 | 注释里原话记录"之前会先发 shutdown notification…会阻塞 60 秒"，且包含退出时间预算的细节 |
| ★★★ | 2C reviewer 解析失败 | "解析不了默认不通过"的安全哲学，面试官最爱听 |
| ★★ | 2A/2B 并行输出与 Worker 竞争 | 两个是配套的：隔离输出 + 池化资源，能讲成一个完整故事 |
| ★★ | 4A KV cache 全 miss | 含"前缀敏感性"原理 + 度量口径，但需要能讲清如何统计 |
| ★★ | 1B 切断 tool 对 | 协议约束思维，适合被追问时补刀 |
| ★★ | 1C 摘要逐级衰减 | "增量更新 vs 从零归纳"的工程直觉 + 二维丢失防线，能连到 extractFacts 讲成一个完整故事 |

### 3. 被追问时的防守话术

**追问："压缩之后模型失忆了怎么办？"**
> 两个兜底：一是 `retainRecentRounds=3` 永远保留最近 3 轮完整消息不压缩，摘要只动旧段；二是摘要 prompt 强制要求保留四类信息（用户诉求、已完成操作、共识、待办），并且摘要失败就跳过本次压缩，绝不带着坏摘要发请求。

**追问："90% 命中率怎么测出来的？"**
> 通过 provider usage API 的 `prompt_cache_hit_tokens` 和 `prompt_cache_miss_tokens` 两个字段，算 `hit / (hit + miss)`，取多轮对话的均值。改造前接近 0，改造后稳定在 90% 以上。同时 `AgentBudget` 里累计了 `cachedInputTokens`，成本估算时缓存 token 用更低的单价。

**追问："多 Agent 的 Reviewer 判断错了怎么办？"**
> 两层防线：解析层——失败默认不通过 + 关键词降级匹配，绝不放行可疑结果；流程层——重试上限 2 次，超限保留当前结果并在汇总里标注，把最终裁决权交还给用户可见的执行总结。

**追问："手写 MCP 为什么不直接用官方 SDK？"**
> 协议本身很薄（JSON-RPC 2.0 上就 initialize / tools/list / tools/call 几个方法），自己写只依赖 Jackson + OkHttp，反而能精准控制 schema 清洗、通知路由异步派发（避免 reader 线程里发请求等自己造成死锁）这些官方 SDK 不替你做的细节。

### 4. 四条避坑提醒

1. **不要背稿，背"结构"不背"台词"**。面试官换个问法你就乱了。把这 8 个 badcase 的四段式骨架记牢，每段现场组织语言。
2. **数字要能自圆其说**。`90%`、`60 个工具`、`2 个 Worker` 这类数字被追问时必须说得出测量方法。说不清的宁可说"接近 0 提升到稳定前缀部分基本全命中"这种定性表述。
3. **区分"我写的"和"设计上已知但未做的"**。比如"角色独立 Skill buffer"在代码注释里明确写了"作为可观察的优化项暂未启用"——面试官如果问起，要主动承认这是简化实现，然后补一句"如果做，我会按角色拆 `SkillContextBuffer` 实例"。**诚实比完美更可信**。
4. **把"失败"讲成"成长"**。每个 badcase 结尾一定要落到复盘（学到了什么），而不是停在"我解决了"。面试官要看到的是你**遇到问题的处理回路**，不只是结果。

---

## 代码位置索引速查

| 技术点 | badcase | 核心代码锚点 |
|---|---|---|
| 双上下文压缩 | 1A / 1B / 1C | `ConversationHistoryCompactor.java`（度量错位注释、user 边界切分）、`ContextCompressor.java`（previousSummary 增量合并、UPDATE_PROMPT、updatePhase、extractFacts 接通）、`ContextProfile.autoCompactTriggerTokens()`、`TokenBudget.estimateMessagesTokens()` |
| 多 Agent 编排 | 2A / 2B / 2C | `AgentOrchestrator.java`（独立缓冲按序 flush、Worker Pool、独立 Reviewer、`parseReviewApproval` 保守策略）、`ExecutionPlan.computeExecutionOrder()` |
| 手写 MCP | 3A / 3B | `McpClient.close()`（shutdown 阻塞 60s 记录）、`StdioTransport.close()`（分级退出）、`McpSchemaSanitizer.sanitize()`、`JsonRpcClient`（id 匹配 + 超时） |
| Prompt 分层 | 4A / 4B | `PromptAssembler.assemble()`（稳定在前、动态在后）、`validateLanguageSection`（fail fast）、`PromptRepository`（三级覆盖） |
