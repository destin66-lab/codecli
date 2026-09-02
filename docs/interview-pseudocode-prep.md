# 闈㈣瘯浼唬鐮佸鎴樻竻鍗曪紙Java 椋庢牸锛?
> 闈㈣瘯瀹樿"鍐欎吉浠ｇ爜璇存槑澶ц嚧娴佺▼"鏃讹紝鏈€鍙兘鎸戣繖鍑犳銆傛寜姒傜巼鎺掑簭锛屾瘡娈电粰浜嗘爣鍑嗗啓娉?+ 杈瑰啓杈硅鐨勫叧閿偣銆?>
> **閫氱敤鍐欐硶鍘熷垯**锛堟瘡鏉″悗闈㈤兘浼氱敤鍒帮級锛?> 1. 鍔ㄧ瑪鍓嶅厛涓€鍙ヨ瘽璇存竻妤?杩欐骞蹭粈涔堛€佹牳蹇冩暟鎹粨鏋勬槸浠€涔?
> 2. 鐢?**Java 椋庢牸鐨勪吉浠ｇ爜**锛氱被鍨嬨€侀泦鍚堬紙`List`/`Map`/`Queue`锛夈€乣CompletableFuture` 杩欎簺鏍稿績绫诲瀷鍐欏嚭鏉ワ紝浣嗕笉鐢ㄧ籂缁撹娉曠粏鑺傦紙import銆佸畬鏁?try-catch銆佹硾鍨嬮€氶厤绗﹂兘鍙互鐪侊級
> 3. 鍏抽敭璁捐鐐圭敤 `// 娉ㄩ噴` 鏍囧嚭鏉モ€斺€旈偅鏄綘鐨勫姞鍒嗛」
> 4. 鎺у埗鍦?20~35 琛岋紝杈瑰啓杈瑰彛澶磋锛屽啓瀹岀珛鍒绘€荤粨涓€鍙?杩欎釜璁捐瑙ｅ喅浜嗕粈涔堥棶棰?

---

## 姒傜巼鎺掑簭

| 鎺掑簭 | 鑰冪偣 | 瀵瑰簲浠ｇ爜 | 姒傜巼 |
|------|------|---------|------|
| 1 | ReAct 涓诲惊鐜?| `Agent.java` | 鈽呪槄鈽呪槄鈽?|
| 2 | 澶?Agent 涓夐樁娈电紪鎺?+ DAG 鍒嗘壒璋冨害 | `AgentOrchestrator.java` | 鈽呪槄鈽呪槄鈽?|
| 3 | 涓婁笅鏂囧帇缂╋紙鍒?user 杈圭晫锛?| `ConversationHistoryCompactor.java` | 鈽呪槄鈽呪槄鈽?|
| 4 | JSON-RPC 璇锋眰/鍝嶅簲閰嶅 | `JsonRpcClient.java` | 鈽呪槄鈽呪槄鈽?|
| 5 | Worker Pool + 鐙珛缂撳啿鎸夊簭 flush | `AgentOrchestrator.runBatchParallel()` | 鈽呪槄鈽呪槅鈽?|
| 6 | Prompt 绋冲畾鍦ㄥ墠鍔ㄦ€佸湪鍚庣粍瑁?| `PromptAssembler.java` | 鈽呪槄鈽呪槅鈽?|

---

## 1. ReAct 涓诲惊鐜紙鏈€鍙兘鑰冿紝鍑犱箮蹇呭啓锛?
**鍏堣**锛?杩欐槸鏁翠釜 Agent 鐨勬牳蹇冿紝涓€涓?while 寰幆锛氭ā鍨嬭瑕佺敤宸ュ叿灏辨墽琛屻€佹妸缁撴灉鍥炵亴銆佸啀闂ā鍨嬶紝鐩村埌妯″瀷缁欏嚭鏈€缁堢瓟妗堛€?

```java
String run(String userInput) {
    List<Message> history = new ArrayList<>();
    history.add(systemMessage(buildSystemPrompt()));   // 鍒嗗眰缁勮濂界殑 system prompt
    history.add(userMessage(userInput));
    AgentBudget budget = AgentBudget.fromLlmClient(llmClient);  // 涓変釜鍏滃簳

    while (true) {
        if (budget.check() != ExitReason.WITHIN_BUDGET) break;  // token/鍋滄粸/杞暟鍏滃簳

        // 璋?LLM 鍓嶏細鍘嗗彶鎺ヨ繎绐楀彛涓婇檺灏卞帇缂?        if (TokenBudget.estimateMessagesTokens(history)
                >= contextProfile.compressionTriggerTokens()) {
            historyCompactor.compactIfNeeded(history, trigger);   // 鍒囧湪 user 杈圭晫
        }

        ChatResponse response = llmClient.chat(history, toolDefinitions);  // SSE 娴佸紡

        if (response.hasToolCalls()) {
            history.add(assistantMessage(response.toolCalls()));
            List<ToolExecutionResult> results = executeToolCalls(response.toolCalls());  // 骞惰
            for (ToolExecutionResult r : results) {
                history.add(toolResult(r.id(), r.result()));   // tool_call/tool_result 鎴愬鍥炵亴
            }
            budget.recordToolCalls(response.toolCalls());      // 鍋滄粸妫€娴?            continue;                                          // 浜ょ粰妯″瀷缁х画鎬濊€?        }
        return response.content();                             // 妯″瀷涓嶅啀璋冨伐鍏?-> 浠诲姟瀹屾垚
    }
    return "瓒呴绠楀己鍒舵敹灏?;
}
```

**杈瑰啓杈硅鐨勫叧閿偣**锛?- 寮鸿皟"**涓诲鏉冨湪妯″瀷**"鈥斺€旀ā鍨嬭繑鍥?content 涓嶈皟宸ュ叿灏辨槸閫€鍑猴紝budget 鍙槸淇濋櫓闃€锛坄AgentBudget.check()` 涓変釜閫€鍑哄師鍥狅細`TOKEN_BUDGET_EXCEEDED` / `STAGNATION_DETECTED` / `HARD_ITERATION_LIMIT`锛?- 鐐瑰嚭 `tool_call` / `tool_result` **鎴愬鍥炵亴**鍒?history锛屾槸鍗忚瀹屾暣鎬х殑鍩虹
- 鎻愬埌鍚屼竴杞涓?tool_call 浼氬苟琛屾墽琛?
---

## 2. 澶?Agent 涓夐樁娈电紪鎺?+ DAG 鍒嗘壒璋冨害

**鍏堣**锛?Planner 鎷嗕换鍔°€丱rchestrator 鎸変緷璧栧垎鎵硅皟搴︺€乄orker 鎵ц銆丷eviewer 瀹℃煡锛屾牳蹇冩槸姣忚疆绛涘嚭'渚濊禆宸叉弧瓒?鐨勬楠ゃ€?

```java
String orchestrate(String task) {
    // 闃舵1锛氳鍒?    AgentMessage plan = planner.execute("鎷嗚В浠诲姟: " + task);
    List<ExecutionStep> steps = parsePlan(plan.content());  // 瑙ｆ瀽鎴?DAG

    // 闃舵2+3锛氭墽琛岋紙鍒嗘壒锛?    while (true) {
        List<ExecutionStep> executable = getExecutableSteps(steps);
        if (executable.isEmpty()) break;

        if (executable.size() == 1) {
            runStepSerial(executable.get(0));   // 鍗曟锛氫覆琛屾祦寮忥紝淇濇寔鎵撳瓧瑙傛劅
        } else {
            runBatchParallel(executable);       // 澶氭锛氱湡姝ｅ苟琛?        }
    }
    return summarize(steps);
}

// 鎷撴墤鍒嗘壒锛氱瓫鍑?鎵€鏈変緷璧栧凡 COMPLETED"鐨?PENDING 姝ラ
List<ExecutionStep> getExecutableSteps(List<ExecutionStep> steps) {
    Map<String, StepStatus> statusMap = ...;   // id -> 褰撳墠鐘舵€?    return steps.stream()
        .filter(s -> s.status() == StepStatus.PENDING)
        .filter(s -> s.dependencies().stream()
            .allMatch(dep -> statusMap.get(dep) == StepStatus.COMPLETED))
        .toList();
}

void runStep(ExecutionStep step) {
    AgentMessage result = worker.executeWithContext(step.description(), context);
    ReviewResult review = reviewer.review(step.description(), result.content());
    if (!review.approved() && retries < 2) {
        // 甯︿笂 issues 鍙嶉閲嶈窇锛屾渶澶?2 娆★紝瓒呰繃淇濈暀褰撳墠缁撴灉
    }
}
```

**杈瑰啓杈硅鐨勫叧閿偣**锛?- 寮鸿皟杩欎笉鏄墜鍐?Kahn 绠楁硶锛岃€屾槸**姣忚疆绛涢€?渚濊禆鍏ㄥ畬鎴愮殑 pending 姝ラ"**锛岃涔夌瓑浠锋嫇鎵戝垎鎵癸紝杩樺ぉ鐒堕伩鍏嶆楠よ閲嶅璋冨害
- 鐐瑰嚭 Planner 鐨?prompt锛坄modes/team-planner.md`锛夐噷灏卞啓鐫€"鐙珛姝ラ涓嶈鍔犱緷璧栵紝璁╃紪鎺掑櫒骞惰"
- 鎻愬埌 `buildStepContext()` 鍙妸**褰撳墠姝ラ渚濊禆鐨勩€佸凡瀹屾垚鐨勬楠?*缁撴灉锛堥瑙?500 瀛楃锛夋敞鍏ヤ笂涓嬫枃锛岄伩鍏嶅叏閲忎俊鎭桨鐐?
---

## 3. 涓婁笅鏂囧帇缂╋紙鍒?user 杈圭晫锛?
**鍏堣**锛?鍘嬬缉鐨勯毦鐐逛笉鏄憳瑕侊紝鑰屾槸鍒囧壊鐐瑰繀椤昏惤鍦?user 娑堟伅杈圭晫锛屽惁鍒欎細鎶?tool_call 鍜屽畠鐨?tool_result 鍒囨柇銆?

```java
boolean compact(List<Message> history, int triggerTokens, int retainRounds) {
    if (TokenBudget.estimateMessagesTokens(history) < triggerTokens) return false;

    // 鍏抽敭1锛氭敹闆嗘墍鏈?user 娑堟伅鐨勭储寮曪紙璺宠繃寮€澶?system锛?    List<Integer> userIndices = new ArrayList<>();
    for (int i = systemEnd; i < history.size(); i++) {
        if ("user".equals(history.get(i).role())) {
            userIndices.add(i);
        }
    }
    if (userIndices.size() <= retainRounds) return false;   // 杞暟澶皯涓嶅帇

    // 鍏抽敭2锛氬垎鍓茬偣鍙栧€掓暟绗?retainRounds 涓?user 绱㈠紩
    int splitIdx = userIndices.get(userIndices.size() - retainRounds);

    String summary = llmSummarize(history.subList(systemEnd, splitIdx));
    if (summary == null) return false;                     // 鎽樿澶辫触锛氳烦杩囨湰杞?
    // 閲嶅缓锛歔system] + [user(鎽樿)] + [assistant(纭)] + [灏鹃儴瀹屾暣淇濈暀]
    List<Message> rebuilt = new ArrayList<>(history.subList(0, systemEnd));
    rebuilt.add(userMessage("[宸插帇缂╃殑鍘嗗彶瀵硅瘽鎽樿]\n" + summary));
    rebuilt.add(assistantMessage("濂界殑锛屾垜宸蹭簡瑙ｄ箣鍓嶇殑涓婁笅鏂囷紝璇风户缁€?));
    rebuilt.addAll(history.subList(splitIdx, history.size()));

    history.clear();
    history.addAll(rebuilt);
    return true;
}
```

**杈瑰啓杈硅鐨勫叧閿偣**锛?- **鍒囧垎鐐逛负浠€涔堝畨鍏?*锛歚splitIdx` 鏉ヨ嚜 user 绱㈠紩闆嗗悎锛屾墍浠ュ垏瀹屼互鍚?history 鏈熬鐨?assistant 娑堟伅**涓嶅彲鑳藉仠鍦?tool_call 涓?*鈥斺€擿tool_call` 鍚庨潰蹇呯劧璺熺潃瀹冪殑 `tool_result`锛屼袱鑰呬竴瀹氬悓灞炰竴涓?user 鍒颁笅涓€涓?user"鐨勫尯闂达紝瑕佷箞閮藉湪鎽樿閲屻€佽涔堥兘鍦ㄥ熬閮?- 鎻掑叆 assistant 纭娑堟伅鏄负浜?*淇濇寔杞瀵归綈**锛屾ā鍨嬩笉浼氱獊鐒剁湅鍒颁竴娈垫憳瑕佽€屽洶鎯?- 鎽樿澶辫触涓嶉樆濉烇細杩斿洖 false 璺宠繃鏈疆锛屼笅涓€杞?token 鏇村鍐嶈瘯
- 鏈€杩?`retainRounds`锛堥粯璁?3锛夎疆瀹屾暣淇濈暀锛屾ā鍨嬭繕鑳藉噯纭搷浣滆繎鏈熷伐鍏?
---

## 4. JSON-RPC 璇锋眰/鍝嶅簲閰嶅

**鍏堣**锛?鏍稿績灏变竴涓暟鎹粨鏋勶細`Map<id, CompletableFuture>`銆傚彂璇锋眰鍓嶆妸 Future 鏀捐繘鍘伙紝鏀跺埌鍝嶅簲鎸?id 鍙栧嚭鏉?complete锛屾病鏈?id 鐨勫氨鏄€氱煡銆?

```java
class JsonRpcClient {
    AtomicLong idSeq = new AtomicLong(1);
    // 鏍稿績鏁版嵁缁撴瀯锛歩d -> 寰呭畬鎴愯姹傦紙骞跺彂瀹夊叏锛?    ConcurrentHashMap<Long, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();
    List<Consumer<JsonNode>> notificationListeners = new CopyOnWriteArrayList<>();

    JsonNode request(String method, JsonNode params, long timeoutSec) throws IOException {
        long id = idSeq.getAndIncrement();
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        pending.put(id, future);

        ObjectNode req = ...;   // {"jsonrpc":"2.0","id":id,"method":method,"params":params}
        transport.send(req);

        scheduleTimeout(id, future, timeoutSec);   // 瓒呮椂锛氫粠 pending 绉婚櫎 + completeExceptionally
        return future.get(timeoutSec + 1, TimeUnit.SECONDS);
    }

    void handleMessage(JsonNode msg) {             // 鐢?transport 鏀跺寘绾跨▼鍥炶皟
        if (!msg.has("id")) {                      // 鏃?id = 鏈嶅姟鍣ㄤ富鍔ㄦ帹閫佺殑閫氱煡
            notificationListeners.forEach(listener -> listener.accept(msg));
            return;
        }
        CompletableFuture<JsonNode> future = pending.remove(msg.get("id").asLong());
        if (future == null) return;                // 宸茶秴鏃?鏈煡 id锛屽拷鐣?        if (msg.has("error")) {
            future.completeExceptionally(new JsonRpcException(msg.get("error")));
        } else {
            future.complete(msg.get("result"));
        }
    }
}
```

**杈瑰啓杈硅鐨勫叧閿偣**锛?- `AtomicLong` 鑷 id + `ConcurrentHashMap` 鏄?*绾跨▼瀹夊叏**鐨勶細transport 鏀跺寘绾跨▼鍜屽彂璧疯姹傜殑绾跨▼骞跺彂鎿嶄綔鍚屼竴涓?Map
- 鍝嶅簲鏄?*寮傛鍒拌揪**鐨勶紙stdio 璇荤嚎绋?/ HTTP 鍥炶皟锛夛紝鎵€浠ュ繀椤荤敤 `CompletableFuture` 妗ユ帴锛屼笉鑳藉湪璇锋眰绾跨▼閲屽悓姝ラ樆濉炵瓑
- 閫氱煡鍜屽搷搴旂殑鍖哄垎灏辨槸**鏈夋病鏈?id 瀛楁**锛岃繖鏄?JSON-RPC 2.0 鐨勮鑼?- 瓒呮椂鐢ㄥ崟鐙皟搴︾嚎绋嬶紝瓒呮椂鍚?`pending.remove(id)` 閬垮厤 Map 娉勬紡

---

## 5. Worker Pool + 鐙珛缂撳啿鎸夊簭 flush

**鍏堣**锛?骞惰鎵ц涓€鎵圭嫭绔嬫楠ゆ椂锛屼笁涓満鍒朵繚璇佹纭€э細Worker 姹犲寲闃茬珵浜夈€佹瘡姝ョ嫭绔嬬紦鍐层€佸畬鎴愬悗鎸夐『搴忚緭鍑恒€?

```java
void runBatchParallel(List<ExecutionStep> batch) {
    // 鏈哄埗1锛歐orker Pool鈥斺€斿悓涓€ worker 涓嶄細琚袱涓楠ゅ苟鍙戝崰鐢?    BlockingQueue<SubAgent> workerPool = new LinkedBlockingQueue<>(workers);
    // 鏈哄埗2锛氭瘡姝ヤ竴涓嫭绔嬪瓧鑺傜紦鍐?    Map<String, ByteArrayOutputStream> buffers = new ConcurrentHashMap<>();
    List<Future<?>> futures = new ArrayList<>();

    for (ExecutionStep step : batch) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buffers.put(step.id(), baos);
        PrintStream stepOut = new PrintStream(baos, true, StandardCharsets.UTF_8);

        futures.add(executor.submit(() -> {
            SubAgent worker = workerPool.take();   // 鍙?worker锛堟嬁涓嶅埌灏遍樆濉炵瓑寰咃紝澶╃劧闄愭祦锛?            try {
                runStep(step, worker, newReviewer(), stepOut);  // 杈撳嚭鍐欒嚜宸辩殑缂撳啿
            } finally {
                workerPool.offer(worker);          // 褰掕繕锛屼笅涓楠ゅ彲澶嶇敤
            }
        }));
    }

    for (Future<?> f : futures) f.get();           // 绛夊叏閮ㄥ畬鎴?
    // 鏈哄埗3锛氭寜 step_id 椤哄簭鎶婄紦鍐?flush 鍒?stdout锛岄伩鍏嶇粓绔瓧绗︿氦閿?    for (ExecutionStep step : batch) {
        out.print(buffers.get(step.id()).toString(StandardCharsets.UTF_8));
    }
}
```

**杈瑰啓杈硅鐨勫叧閿偣**锛?- `workerPool.take()` 鎷夸笉鍒颁細**闃诲绛夊緟**锛屽ぉ鐒堕檺娴佲€斺€旀睜閲屽彧鏈?2 涓?worker锛屽悓鏃舵墽琛岀殑姝ラ鏈€澶?2 涓?- 鐩存帴鍐?`System.out` 浼氬绾跨▼浜ら敊锛屾墍浠ユ瘡姝ュ啓鑷繁鐨勭紦鍐层€佹渶鍚庢寜椤哄簭缁熶竴杈撳嚭
- Reviewer 涔熸槸姣忎釜骞惰姝ラ**鏂板缓鐙珛瀹炰緥**锛坄new SubAgent("reviewer-" + step.id(), ...)`锛夛紝閬垮厤鍏变韩瀵硅瘽鍘嗗彶瀵艰嚧绔炰簤
- 涓茶璺緞锛堝崟姝ワ級涓嶈蛋缂撳啿锛岀洿鎺ユ祦寮忚緭鍑猴紝淇濇寔"瀹炴椂鎵撳瓧"鐨勪綋楠?
---

## 6. Prompt 鍒嗗眰缁勮锛堢ǔ瀹氬湪鍓嶅姩鎬佸湪鍚庯級

**鍏堣**锛?system prompt 鎸夌ǔ瀹氬害鎺掑簭鎷兼帴锛岀ǔ瀹氭鍦ㄥ墠鏋勬垚 KV cache 鍓嶇紑锛屽姩鎬佹鍦ㄥ悗姣忚疆閲嶇畻锛屾渶鍚庡啀鏀跺熬涓€娈电ǔ瀹氭銆?

```java
String assemble(PromptMode mode, PromptContext ctx) {
    StringBuilder sb = new StringBuilder();
    // 鈥斺€?绋冲畾娈碉紙KV cache 鍓嶇紑锛屼細璇濇湡闂翠笉鍙橈級鈥斺€?    append(sb, repository.loadRequired("base.md"));                    // 韬唤/璇█/宸ュ叿/绛栫暐
    append(sb, repository.loadRequired("personalities/calm.md"));      // 鎬ф牸
    append(sb, applyVariables(repository.loadRequired(mode.resourcePath()), ctx)); // 妯″紡
    append(sb, repository.loadRequired("approvals/" + approvalMode(ctx) + ".md"));
    // 鈥斺€?鍗婂姩鎬侊細鏃ユ湡/鏃跺尯锛岃法澶╂墠鍙?鈥斺€?    append(sb, runtimeContext());
    // 鈥斺€?鍔ㄦ€侊細姣忚疆鍙橈紝鏀炬湯灏句笉褰卞搷鍓嶇紑 鈥斺€?    append(sb, dynamicSection("Project Context",
            ctx.projectMemoryContext(), ctx.memoryContext(), ctx.externalContext()));
    append(sb, dynamicSection("Skills", ctx.skillIndex()));
    // 鈥斺€?鏀跺熬绋冲畾娈?鈥斺€?    append(sb, repository.loadRequired("context/context-management.md"));
    append(sb, repository.loadRequired("handoff.md"));
    return sb.toString().trim();
}
```

**杈瑰啓杈硅鐨勫叧閿偣**锛?- **涓轰粈涔堣繖鑳芥彁鍗囧懡涓巼**锛歱rovider 鐨勫墠缂€缂撳瓨瑕佹眰鍓嶇紑閫愬瓧鑺備竴鑷淬€傛棫鐗堟妸璁板繂妫€绱㈢粨鏋滄贩鍦ㄤ腑闂达紝妫€绱㈢粨鏋滀竴鍙樸€佹暣涓墠缂€閮藉け鏁堬紱鐜板湪绋冲畾娈靛墠缃€佸姩鎬佹鎸埌鏈熬锛?*鍙绋冲畾娈典笉鍙橈紝cache 灏卞懡涓?*锛屽彧閲嶇畻鏈熬鍔ㄦ€佹
- 涓夌骇瑕嗙洊椤哄甫涓€鎻愶細`loadRequired()` 浼氫粠 JAR 鍐呯疆 鈫?`~/.CodeCLI/prompts/` 鈫?椤圭洰 `.CodeCLI/prompts/` 閫愬眰瑕嗙洊锛岀敤鎴锋敼 Markdown 灏辫兘瀹氬埗琛屼负銆佷笉鐢ㄦ敼浠ｇ爜

---

## 闄勶細琚拷闂椂鐨?涓€鍙ヨ瘽鎷旈珮"

鍐欏畬姣忔浼唬鐮侊紝濡傛灉闈㈣瘯瀹樿拷闂紝鐢ㄤ竴鍙ヨ瘽鏀跺熬锛?
| 杩介棶鏂瑰悜 | 涓€鍙ヨ瘽鍥炵瓟 |
|---------|-----------|
| "涓轰粈涔堣繖涔堣璁★紵" | 浠?*鍙栬垗**瑙掑害绛旓細鍘嬬缉鏄?淇℃伅鏃犳崯 vs token 鑺傜渷"鐨勬潈琛★紝鎵€浠ヤ繚鐣欐渶杩?N 杞?+ 鍙帇鏃ф秷鎭紱骞惰鏄?鍚炲悙 vs 杈撳嚭鏈夊簭"鐨勬潈琛★紝鎵€浠ョ嫭绔嬬紦鍐?鎸夊簭 flush銆?|
| "鍝噷浼氬嚭闂锛? | 涓诲姩璇?*宸茬煡杈圭晫**锛氭憳瑕?LLM 澶辫触灏辫烦杩囨湰杞紱Reviewer 瑙ｆ瀽澶辫触榛樿鍒や笉閫氳繃锛堜繚瀹堬級锛涢€氱煡 handler 鏀剧嫭绔嬬嚎绋嬫淳鍙戦槻姝?鑷繁绛夎嚜宸辩殑鍝嶅簲"姝婚攣銆?|
| "鎬庝箞娴嬭瘯锛? | 鎻愬埌娴嬭瘯锛歚MainInputNormalizationTest`銆乣McpSchemaSanitizerTest` 绛夊崟娴嬶紱鍘嬬缉鍣ㄧ殑 `summarize()` 鏄?`protected` 鏂规硶锛屾祴璇曞彲浠ラ€氳繃瀛愮被鏇挎崲鎺夌湡瀹?LLM 璋冪敤锛汚gentOrchestrator 鐨?`ExecutionStep`/`getExecutableSteps()` 鏄?package-private锛屼緵娴嬭瘯鐩存帴璁块棶銆?|
| "杩樿兘鎬庝箞浼樺寲锛? | 鍘嬬缉鍓嶅厛璇勪及"鎽樿鎴愭湰 vs 鏀剁泭"锛堟憳瑕佹湰韬鑺?token锛夛紱骞惰鎵规缁?`f.get(timeout)` 鍔犺秴鏃讹紱Prompt 鍔ㄦ€佹鍐嶅仛涓€娆″眬閮ㄧ紦瀛樸€?|

