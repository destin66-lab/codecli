# 绠€鍘嗘妧鏈偣闈㈣瘯鍑嗗

> 鏈枃妗ｅ皢浣犵殑绠€鍘嗗洓澶ф妧鏈偣閫愪竴鏄犲皠鍒颁唬鐮佸疄鐜帮紝鐢?闈㈣瘯瀹樿瑙?鍒嗘瀽姣忎釜鐐瑰彲浠ヨ鎬庝箞闂€佷綘搴旇鎬庝箞绛斻€?
---

## 鐩綍

1. [鍙屼笂涓嬫枃鍘嬬缉涓庨暱浼氳瘽绠＄悊](#1-鍙屼笂涓嬫枃鍘嬬缉涓庨暱浼氳瘽绠＄悊)
2. [澶?Agent 鍗忎綔涓庡苟琛岃皟搴(#2-澶?agent-鍗忎綔涓庡苟琛岃皟搴?
3. [鎵嬪啓 MCP 鍗忚涓庡伐鍏风敓鎬侀泦鎴怾(#3-鎵嬪啓-mcp-鍗忚涓庡伐鍏风敓鎬侀泦鎴?
4. [Prompt 鍒嗗眰鏋舵瀯涓?KV Cache 浼樺寲](#4-prompt-鍒嗗眰鏋舵瀯涓?kv-cache-浼樺寲)
5. [MCP 宸ュ叿瀹炰緥璁茶В锛氭帴鍏ヤ簡鍝簺 + chrome-devtools 鍏ㄦ祦绋媇(#5-mcp-宸ュ叿瀹炰緥璁茶В鎺ュ叆浜嗗摢浜?-chrome-devtools-鍏ㄦ祦绋?

---

## 1. 鍙屼笂涓嬫枃鍘嬬缉涓庨暱浼氳瘽绠＄悊

### 绠€鍘嗗師璇?
> 璁捐娑堟伅鍒楄〃鍘嬬缉涓庣煭鏈熻蹇嗘憳瑕佸帇缂╀袱閬撴満鍒讹紝鍘嬬缉鍒嗗壊鐐瑰己鍒惰惤鍦?user message 杈圭晫閬垮厤鍒囨柇 toolCall/toolResult 鎴愬鍗忚锛涜嚜鍔ㄥ帇缂╅槇鍊奸€氳繃绾嚱鏁版淳鐢燂紝鏂版ā鍨嬮浂閰嶇疆閫傞厤銆?
### 浠ｇ爜瀹炵幇

#### 绗竴閬擄細娑堟伅鍒楄〃鍘嬬缉 鈥?`ConversationHistoryCompactor`

**鏂囦欢**: `src/main/java/com/CodeCLI/memory/ConversationHistoryCompactor.java`

**浣滅敤**: 鍘嬬缉 Agent 瀹為檯鍙戠粰 LLM 鐨?`conversationHistory`锛坄List<LlmClient.Message>`锛夛紝鐩存帴鍐冲畾涓嬩竴杞緭鍏?token 鏄惁涓嬮檷銆?
**鏍稿績鍘嬬缉绠楁硶** (`compact()` 鏂规硶锛岀害绗?70 琛?:

```java
// 1. 浼扮畻鎬?token锛屾湭杈鹃槇鍊肩洿鎺ヨ繑鍥?false
int currentTokens = TokenBudget.estimateMessagesTokens(history);
if (!force && currentTokens < triggerTokens) return false;

// 2. 鎵惧嚭鎵€鏈?user message 鐨勭储寮曪紝淇濈暀鏈€杩?retainRecentRounds 杞?List<Integer> userIndices = new ArrayList<>();
for (int i = systemEnd; i < history.size(); i++) {
    if ("user".equals(history.get(i).role())) {
        userIndices.add(i);
    }
}
int splitIdx = userIndices.get(userIndices.size() - effectiveRetainRounds);

// 3. 鎶?system 涔嬪悗銆乻plitIdx 涔嬪墠鐨勬秷鎭叏閮ㄥ杺缁?LLM 鍋氭憳瑕?List<LlmClient.Message> oldMsgs = history.subList(systemEnd, splitIdx);
String summary = summarize(oldMsgs);  // 璋?LLM 鐢熸垚鎽樿

// 4. 閲嶅缓锛歔system] + [user(鎽樿)] + [assistant(纭)] + [灏鹃儴淇濈暀娑堟伅]
rebuilt.add(LlmClient.Message.user("[宸插帇缂╃殑鍘嗗彶瀵硅瘽鎽樿]\n" + summary));
rebuilt.add(LlmClient.Message.assistant("濂界殑锛屾垜宸蹭簡瑙ｄ箣鍓嶇殑涓婁笅鏂囷紝璇风户缁€?));
rebuilt.addAll(history.subList(splitIdx, history.size()));
```

**鍏抽敭璁捐**: 鍒嗗壊鐐?`splitIdx` 鏉ヨ嚜 `userIndices` 鍒楄〃锛屽繀鐒惰惤鍦?user message 杈圭晫锛屼笉浼氭妸 `tool_call` / `tool_result` 鎴愬鍗忚鍒囨柇銆?
#### 绗簩閬擄細鐭湡璁板繂鎽樿鍘嬬缉 鈥?`ContextCompressor`

**鏂囦欢**: `src/main/java/com/CodeCLI/memory/ContextCompressor.java`

**浣滅敤**: 鍘嬬缉 `ConversationMemory`锛圥aiCLI 鐨勭煭鏈熻蹇嗘潯鐩級锛岄噰鐢?Map-Reduce 绛栫暐銆?
**鏍稿績绠楁硶** (`compress()`, 绾︾ 100 琛?:

```java
// 鍒嗗壊锛氭棫娑堟伅 vs 杩戞湡娑堟伅
int splitPoint = allEntries.size() - retainRecentRounds;
List<MemoryEntry> oldEntries = new ArrayList<>(allEntries.subList(0, splitPoint));
List<MemoryEntry> recentEntries = new ArrayList<>(allEntries.subList(splitPoint, allEntries.size()));

// Map 闃舵锛? 鏉′竴缁勫垎鐗囨憳瑕?List<String> chunkSummaries = mapPhase(oldEntries);
// Reduce 闃舵锛氬悎骞跺涓墖娈垫憳瑕?String finalSummary = reducePhase(chunkSummaries);

// 娓呯┖鏃ц蹇嗭紝娉ㄥ叆鎽樿+淇濈暀杩戞湡璁板繂
memory.clear();
memory.store(summaryEntry);    // 鎽樿鏉＄洰
for (MemoryEntry entry : recentEntries) {
    memory.store(entry);       // 鎭㈠杩戞湡鏉＄洰
}
```

杩欎釜绫昏繕璐熻矗鎻愬彇闀挎湡璁板繂浜嬪疄 (`extractFacts()`)锛岀敤 `EPHEMERAL_FACT_PREFIXES` / `DURABLE_FACT_HINTS` 绛夊惎鍙戝紡瑙勫垯杩囨护涓存椂鎬у唴瀹癸紝鍙繚鐣欒法浼氳瘽浠嶇劧鎴愮珛鐨勭ǔ瀹氫簨瀹炪€?
#### 鑷姩鍘嬬缉闃堝€?鈥?`ContextProfile`

**鏂囦欢**: `src/main/java/com/CodeCLI/context/ContextProfile.java`

**璁捐鍘熷垯**: 娌℃湁"闀?鐭?骞宠　"妯″紡鍒嗘。銆傛墍鏈夊弬鏁伴兘鏄?`maxContextWindow` 鐨勭函鍑芥暟锛屽叏妯″瀷璧板悓涓€濂楄涓猴紝鍙槸绐楀彛澶у皬涓嶅悓瀵艰嚧瑙﹀彂鏃舵満鍜屽閲忎笉鍚屻€?
```java
public static ContextProfile from(LlmClient llmClient) {
    int window = llmClient.maxContextWindow();  // 鏂版ā鍨嬪彧瑕佸疄鐜拌繖涓€涓柟娉?    return new ContextProfile(
        window,
        agentBudget(window),           // window 脳 0.8
        compressionTriggerRatio(window), // 鑷姩娲剧敓
        shortTermBudget(window),       // window 脳 0.45
        memoryContextTokens(window),   // window / 200, 灏侀《 5000
        window >= 32_000,              // MCP resource 绱㈠紩寮€鍏?        llmClient.supportsPromptCaching(),  // 鏂版ā鍨嬭兘鍔涙爣蹇?        llmClient.promptCacheMode()
    );
}
```

**鍘嬬缉瑙﹀彂闃堝€艰绠?* (`autoCompactTriggerTokens()`):

```java
private static int autoCompactTriggerTokens(int window) {
    int summaryReserve = min(window / 4, 20_000);  // 棰勭暀鎽樿杈撳嚭绌洪棿
    int buffer = min(window / 8, 13_000);           // 鑷姩鍘嬬缉缂撳啿
    int trigger = window - summaryReserve - buffer;
    return max(1_000, min(window - 1, trigger));
}
```

**鏂版ā鍨嬮€傞厤**: 鍙渶瑕佸疄鐜?`maxContextWindow()`銆乣supportsPromptCaching()`銆乣promptCacheMode()` 涓変釜鏂规硶锛屽帇缂╅槇鍊笺€佽蹇嗛绠椼€丮CP resource 寮€鍏冲叏閮ㄨ嚜鍔ㄨ窡闅忋€?
### 闈㈣瘯鍥炵瓟

> **闈㈣瘯瀹?*: 浣犵畝鍘嗛噷鎻愬埌鐨?鍙屼笂涓嬫枃鍘嬬缉"鍏蜂綋鏄粈涔堬紵涓轰粈涔堥渶瑕佷袱濂楋紵

**鏍囧噯鍥炵瓟**:

"CodeCLI 缁存姢浜嗕袱濂楀苟琛岀殑娑堟伅瀛樺偍锛?
1. **鐭湡璁板繂** (`ConversationMemory`) 鈥?浠?`MemoryEntry` 涓哄崟浣嶇殑缁撴瀯鍖栬蹇嗭紝鍖呭惈 metadata銆佺被鍨嬫爣绛剧瓑锛屼緵璁板繂妫€绱㈠櫒浣跨敤
2. **瀵硅瘽鍘嗗彶** (`conversationHistory`) 鈥?Agent 瀹為檯鍙戠粰 LLM 鐨?`List<LlmClient.Message>`锛屾槸鐪熸娑堣€?token 鐨勮浇浣?
涓ゅ瀛樺偍鐨勭敤閫斾笉鍚岋紝鎵€浠ラ渶瑕佺嫭绔嬬殑鍘嬬缉鍣細

- **`ContextCompressor`** 鍘嬬缉鐭湡璁板繂锛岄噰鐢?Map-Reduce 鍒嗙墖鎽樿绛栫暐锛屼繚鐣欐渶杩?N 杞畬鏁存潯鐩笉鏀剧缉锛屽彧鍘嬬缉鏃х墖娈?- **`ConversationHistoryCompactor`** 鍘嬬缉瀵硅瘽鍘嗗彶锛岃繖鏄洿鍏抽敭鐨勪竴灞傗€斺€斿洜涓哄畠鐩存帴鍐冲畾涓嬩竴杞?LLM 璋冪敤鐨勮緭鍏?token 鏄惁鍑忓皯

涓ゅ眰鐨勫叡鍚岀偣锛氬垎鍓茬偣閮藉己鍒惰惤鍦?user message 杈圭晫锛岀‘淇?`tool_call` / `tool_result` 鐨勬垚瀵瑰崗璁笉琚垏鏂€俙ConversationHistoryCompactor` 閫氳繃鎵弿鎵€鏈?user 娑堟伅鐨勭储寮曟潵鍐冲畾鍒囧垎浣嶇疆锛宍ContextCompressor` 鍒欐寜鏉＄洰鏁伴噺淇濈暀灏鹃儴銆?
鍘嬬缉闃堝€兼柟闈紝`ContextProfile` 閲囩敤绾嚱鏁版淳鐢熺瓥鐣ワ細鎵€鏈夊弬鏁帮紙鍘嬬缉瑙﹀彂闃堝€笺€佺煭鏈熻蹇嗛绠椼€佽蹇嗕笂涓嬫枃 token 鏁帮級閮芥槸 `maxContextWindow` 鐨勭函鍑芥暟锛屾病鏈夌‖缂栫爜鐨?闀跨煭骞宠　"妯″紡鍒嗘。銆傛柊妯″瀷鍙瀹炵幇 `maxContextWindow()` 鍜?`supportsPromptCaching()` 涓や釜鎺ュ彛鏂规硶锛屽帇缂╄涓鸿嚜鍔ㄩ€傞厤锛屼笉闇€瑕佷汉宸ユ敼閰嶇疆銆?

### 鍙兘鐨勮拷闂?
| 杩介棶 | 寤鸿鍥炵瓟 |
|------|---------|
| **涓轰粈涔堝帇缂╂憳瑕佸悗瑕佹彃鍏ヤ竴鏉?assistant 鐨勭‘璁ゆ秷鎭紵** | 璁?LLM 鍦ㄤ笅涓€杞湅鍒?鐢ㄦ埛缁欎簡涓€娈垫憳瑕?+ 鎴戣鐭ラ亾浜?鐨勫畬鏁村璇濈粨鏋勶紝鑰屼笉鏄獊鐒跺嚭鐜颁竴娈垫憳瑕併€傝繖淇濇寔浜嗗璇濊疆娆＄殑瀵归綈锛屾ā鍨嬩笉浼氬洜涓?涓婁竴杞垜杩樺湪鍐欎唬鐮侊紝杩欎竴杞獊鐒跺彉鎴愭憳瑕?鑰屽洶鎯戙€?|
| **鍘嬬缉鏃?LLM 鎽樿璋冪敤濡傛灉澶辫触浜嗘€庝箞澶勭悊锛?* | 涓や釜鍘嬬缉鍣ㄩ兘 try-catch 浜?LLM 璋冪敤銆俙ConversationHistoryCompactor` 濡傛灉鎽樿澶辫触锛宭og warning 骞?`return false`锛岃烦杩囨湰杞帇缂╋紝涓嬩竴杞?token 鏇村鏃跺啀璇曘€俙ContextCompressor` 鍚屾牱杩斿洖 null锛屼笂灞?`MemoryManager.compressIfNeeded()` 妫€鏌ュ埌 null 灏变笉鍋氭浛鎹€?|
| **涓轰粈涔?ContextProfile 涓嶇敤閰嶇疆锛岃€屾槸绾嚱鏁帮紵** | 鏍稿績鍘熷洜鏄?CodeCLI 鏀寔 7 涓?LLM provider锛屾瘡涓彲鑳芥湁澶氭妯″瀷锛屼笉鍚屾ā鍨嬬殑涓婁笅鏂囩獥鍙ｄ粠 32k 鍒?1M 涓嶇瓑銆傚鏋滈潬浜哄伐閰嶆ā寮忥紝姣忔柊澧炰竴涓ā鍨嬪氨瑕佹敼閰嶇疆锛屼笉鍙淮鎶ゃ€傜函鍑芥暟绛栫暐璁╂墍鏈夋ā鍨嬭蛋鍚屼竴濂楄涓洪€昏緫锛屽彧鏄獥鍙ｅぇ灏忎笉鍚屽鑷磋Е鍙戞椂鏈哄拰瀹归噺涓嶅悓銆?|
| **"闀挎湡璁板繂"鍜?鐭湡璁板繂"鎬庝箞鍖哄垎锛?* | 鐭湡璁板繂: 褰撳墠浼氳瘽鐨?`MemoryEntry` 鍒楄〃锛屾湁 token 棰勭畻闄愬埗锛岃秴闃堝€兼椂琚?ContextCompressor 鍘嬬缉銆傞暱鏈熻蹇? 鎸佷箙鍖栧埌纾佺洏 JSON 鏂囦欢鐨?`LongTermMemory`锛坄long_term_memory.json`锛夛紝鐢?`save_memory` 宸ュ叿鍐欏叆锛岃法浼氳瘽鍙绱€侰ontextCompressor 鐨?`extractFacts()` 鏂规硶浼氫粠鐭湡璁板繂涓彁鍙栫ǔ瀹氫簨瀹炴帹鍏ラ暱鏈熻蹇嗐€?|

---

## 2. 澶?Agent 鍗忎綔涓庡苟琛岃皟搴?
### 绠€鍘嗗師璇?
> 瀹炵幇 Planner->Worker脳N->Reviewer 涓夐樁娈电紪鎺掞紝閫氳繃鎷撴墤鎺掑簭鏋勫缓 DAG 渚濊禆鍥惧垎鎵硅皟搴︼紱璁捐 Worker Pool 閬垮厤骞跺彂绔炰簤锛岀嫭绔嬬紦鍐叉寜搴?flush 淇濊瘉杈撳嚭涓嶄贡搴忋€?
### 浠ｇ爜瀹炵幇

#### 涓夐樁娈电紪鎺?鈥?`AgentOrchestrator`

**鏂囦欢**: `src/main/java/com/CodeCLI/agent/AgentOrchestrator.java`

**鏍稿績娴佺▼** (`run()` 鏂规硶锛岀害绗?130 琛?:

```java
// 1. 瑙勫垝闃舵锛歅lanner 浜у嚭 JSON 璁″垝
out.println("馃搵 绗竴闃舵锛氳鍒?);
AgentMessage planResult = planner.execute(planMessage, out);

// 2. 瑙ｆ瀽璁″垝锛欽SON 鈫?DAG 鍥剧粨鏋?List<ExecutionStep> steps = parsePlan(planResult.content());

// 3. 鎵ц闃舵锛氭寜渚濊禆鍒嗘壒璋冨害
while (true) {
    List<ExecutionStep> executable = getExecutableSteps(steps);
    // 姣忔鍙?鎵€鏈変緷璧栧凡婊¤冻"鐨勬楠?    if (executable.isEmpty()) break;

    if (executable.size() == 1) {
        // 鍗曟锛氫覆琛屾祦寮忥紝淇濇寔瀹炴椂鎵撳瓧瑙傛劅
        runStep(step, steps, retryCount, worker, reviewer, context, out);
    } else {
        // 澶氭锛氱湡姝ｅ苟琛屾墽琛?        runBatchParallel(executable, steps, retryCount);
    }
}
```

#### DAG 渚濊禆鍥炬瀯寤?鈥?`parsePlan()`

**鏂囦欢**: `AgentOrchestrator.java` 绾︾ 245 琛?
```java
List<ExecutionStep> parsePlan(String planJson) {
    JsonNode root = mapper.readTree(cleaned);
    JsonNode stepsNode = root.path("steps");  // 鎴?"tasks" 瀛楁

    // 绗竴閬嶏細鍒涘缓姝ラ锛岄噸缂栧彿 id
    for (JsonNode stepNode : stepsNode) {
        steps.add(ExecutionStep.pending(newId, description, type, new ArrayList<>()));
    }
    // 绗簩閬嶏細寤虹珛渚濊禆鍏崇郴
    for (JsonNode stepNode : stepsNode) {
        // 浠?dependencies 鏁扮粍瑙ｆ瀽渚濊禆鐨?step id
        steps.set(idx, new ExecutionStep(..., deps, ...));
    }
    return steps;
}
```

#### 鎷撴墤鎺掑簭鍒嗘壒璋冨害 鈥?`getExecutableSteps()`

```java
List<ExecutionStep> getExecutableSteps(List<ExecutionStep> steps) {
    Map<String, StepStatus> statusMap = ...;  // 褰撳墠鎵€鏈夋楠ょ殑鐘舵€?    return steps.stream()
        .filter(step -> step.status() == PENDING)
        .filter(step -> step.dependencies().stream()
            .allMatch(dep -> statusMap.get(dep) == COMPLETED))
        .toList();  // 鍗?鎵€鏈変緷璧栧凡瀹屾垚鐨勫緟鎵ц姝ラ"
}
```

**娉ㄦ剰**: 杩欎笉鏄樉寮忕殑 Kahn 鎷撴墤鎺掑簭锛岃€屾槸姣忚疆浠?pending 姝ラ涓瓫閫?渚濊禆宸插叏閮ㄥ畬鎴?鐨勬楠わ紝绛変环浜庢嫇鎵戞帓搴忕殑閫愭壒璋冨害銆?
#### Worker Pool + 鐙珛缂撳啿鎸夊簭 flush 鈥?`runBatchParallel()`

**鏂囦欢**: `AgentOrchestrator.java` 绾︾ 390 琛?
```java
private void runBatchParallel(List<ExecutionStep> batch, List<ExecutionStep> steps, ...) {
    // Worker Pool锛欱lockingQueue 淇濊瘉鍚屼竴 Worker 涓嶈涓や釜姝ラ骞跺彂鍗犵敤
    BlockingQueue<SubAgent> workerPool = new LinkedBlockingQueue<>(workers);

    // 鐙珛缂撳啿锛氭瘡涓楠や竴涓?ByteArrayOutputStream
    Map<String, ByteArrayOutputStream> buffers = new ConcurrentHashMap<>();

    for (ExecutionStep step : batch) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buffers.put(step.id(), baos);
        PrintStream stepOut = new PrintStream(baos, true, UTF_8);

        futures.add(executor.submit(() -> {
            SubAgent worker = workerPool.take();  // 浠庢睜涓彇
            try {
                runStep(step, steps, retryCount, worker, localReviewer, context, stepOut);
            } finally {
                workerPool.offer(worker);  // 褰掕繕
            }
        }));
    }

    // 绛夊緟鎵€鏈夊苟琛屼换鍔″畬鎴?    for (Future<?> f : futures) f.get();

    // 鎸?step_id 椤哄簭 flush 缂撳啿杈撳嚭
    for (ExecutionStep step : batch) {
        ByteArrayOutputStream buf = buffers.get(step.id());
        if (buf != null && buf.size() > 0) {
            out.print(buf.toString(UTF_8));
        }
    }
}
```

#### Reviewer 閲嶈瘯鏈哄埗 鈥?`runStep()`

**鏂囦欢**: `AgentOrchestrator.java` 绾︾ 450 琛?
```java
// Worker 鎵ц
AgentMessage result = worker.executeWithContext(taskMsg, context, out);

// Reviewer 瀹℃煡
AgentMessage reviewResult = reviewer.review(step.description(), result.content(), out);

// 涓嶉€氳繃 鈫?鏈€澶氶噸璇?2 娆?while (!approved && retries < MAX_RETRIES_PER_STEP) {
    retries++;
    AgentMessage retryResult = worker.executeWithContext(taskMsg, feedbackContext, out);
    AgentMessage retryReview = reviewer.review(step.description(), acceptedResult, out);
}
```

#### 骞惰姝ラ鐨勭嫭绔?Reviewer

```java
// 姣忎釜骞惰姝ラ閮?new 涓€涓嫭绔嬬殑 Reviewer锛岄伩鍏嶅璇濆巻鍙茬珵浜?SubAgent localReviewer = new SubAgent("reviewer-" + step.id(), REVIEWER, llmClient, toolRegistry);
```

### 闈㈣瘯鍥炵瓟

> **闈㈣瘯瀹?*: 浣犳€庝箞瀹炵幇澶?Agent 鍗忎綔鐨勶紵Planner銆乄orker銆丷eviewer 涔嬮棿鎬庝箞閫氫俊锛?
**鏍囧噯鍥炵瓟**:

"CodeCLI 鐨?Multi-Agent 閲囩敤 Planner鈫扺orker脳N鈫扲eviewer 涓夐樁娈电紪鎺掞細

1. **Planner** 鍏堝垎鏋愮敤鎴烽渶姹傦紝杈撳嚭 JSON 鏍煎紡鐨勬墽琛岃鍒掞紝鍖呭惈姝ラ鍒楄〃鍜屼緷璧栧叧绯汇€侾lanner 鐨?prompt 鍦?`prompts/modes/team-planner.md` 涓紝鏄庣‘瑕佹眰"澶氫釜姝ラ鍙互鐙珛瀹屾垚鏃讹紝涓嶈娣诲姞渚濊禆锛岃缂栨帓鍣ㄥ苟琛屽垎閰?銆?
2. **Orchestrator** 瑙ｆ瀽 JSON 璁″垝锛屾瀯寤?`ExecutionStep` 鍒楄〃锛屾瘡姝ュ寘鍚?`id`銆乣description`銆乣dependencies` 瀛楁銆俙getExecutableSteps()` 姣忚疆浠?pending 姝ラ涓瓫閫?鎵€鏈変緷璧栧凡瀹屾垚鐨勬楠?鈥斺€旂瓑浠蜂簬鎷撴墤鎺掑簭鐨勯€愭壒璋冨害銆?
3. **Worker** 鎵ц鍏蜂綋姝ラ銆傛瘡涓?Worker 鏄嫭绔嬬殑 `SubAgent` 瀹炰緥锛屾湁鑷繁鐨勫璇濆巻鍙诧紝浣嗗叡浜?LLM 瀹㈡埛绔拰宸ュ叿娉ㄥ唽琛ㄣ€?
4. **Reviewer** 瀹℃煡 Worker 鐨勭粨鏋滐紝杈撳嚭 `approved`/`issues`銆傚鏋滃鏌ヤ笉閫氳繃锛學orker 鏈€澶氶噸璇?2 娆°€?
骞惰璋冨害鏂归潰锛屾垜璁捐浜嗕笁涓満鍒讹細

- **Worker Pool**: 鐢?`BlockingQueue<SubAgent>` 姹犲寲鍒嗛厤锛岀‘淇濆悓涓€ Worker 涓嶄細琚袱涓楠ゅ苟鍙戝崰鐢ㄣ€俙take()` 鑾峰彇锛宍offer()` 褰掕繕銆?- **鐙珛缂撳啿**: 姣忎釜骞惰姝ラ鍐欏叆鑷繁鐨?`ByteArrayOutputStream`锛岄伩鍏嶅绾跨▼鍐欏悓涓€涓粓绔祦閫犳垚瀛楃浜ら敊銆?- **鎸夊簭 flush**: 鎵€鏈夊苟琛屼换鍔″畬鎴愬悗锛屾寜 `step_id` 椤哄簭灏嗙紦鍐插尯鍐呭 flush 鍒?stdout锛岀敤鎴风湅鍒扮殑鎵ц杩囩▼鏈夌ǔ瀹氱殑椤哄簭銆?
姝ゅ锛屾瘡涓苟琛屾楠ら兘 new 涓€涓嫭绔嬬殑 Reviewer 瀹炰緥锛岄伩鍏嶅涓楠ゅ叡浜悓涓€涓璇濆巻鍙插鑷寸珵浜夋潯浠躲€?

### 鍙兘鐨勮拷闂?
| 杩介棶 | 寤鸿鍥炵瓟 |
|------|---------|
| **涓轰粈涔堜笉鐢ㄦ樉寮忕殑 Kahn 鎷撴墤鎺掑簭锛?* | 鍥犱负 `getExecutableSteps()` 姣忚疆鍒烽€?渚濊禆宸插叏閮ㄥ畬鎴愮殑 PENDING 姝ラ"鍦ㄨ涔変笂绛変环浜庢嫇鎵戞帓搴忥紝鑰屼笖鏇寸畝娲佲€斺€斾笉闇€瑕佺淮鎶ゅ叆搴︽暟缁勫拰闃熷垪锛岀姸鎬佺洿鎺ユ潵鑷?`ExecutionStep.status` 瀛楁銆備篃閬垮厤浜嗘楠よ閲嶅璋冨害鐨勯棶棰樸€?|
| **Worker Pool 鐨勫閲忔€庝箞纭畾锛?* | 榛樿 2 涓?Worker锛屾瀯閫犲嚱鏁颁腑 `List.of(new SubAgent("worker-1", ...), new SubAgent("worker-2", ...))`銆傝繖鏄竴涓粡楠屽€硷細骞惰搴﹀お楂樹細绔炰簤 LLM 鐨?API 閫熺巼闄愬埗锛屽お浣庡垯鏃犳硶鍒╃敤骞惰浼樺娍銆? 涓?Worker 鍦ㄥぇ澶氭暟鍦烘櫙涓嬭揪鍒拌緝濂界殑骞宠　銆?|
| **濡傛灉鏌愪釜 Worker 鎵ц瓒呮椂鎬庝箞鍔烇紵** | 鐩墠骞惰鎵规娌℃湁鐙珛鐨勮秴鏃舵帶鍒讹紝ExecutorService 鐨?`invokeAll` 鎴?`f.get()` 浼氫竴鐩寸瓑寰呫€傚鏋滅‘瀹為渶瑕佽秴鏃讹紝鍙互鍦?`runStep()` 鍐呴儴閫氳繃 `CancellationContext` 鎴栫粰 `f.get(timeout, unit)` 鍔犺秴鏃躲€傜洰鍓嶇殑璁捐鍋忓悜浜?璁?Worker 鑷劧瀹屾垚"銆?|
| **Reviewer 濡傛灉璇垽锛堣杩囪涓嶈繃锛岃涓嶈繃璇磋繃锛夋€庝箞鍔烇紵** | `parseReviewApproval()` 鍋氫簡淇濆畧璁捐锛氳В鏋愬け璐ユ椂榛樿鍒や负"涓嶉€氳繃"銆傚鏋?JSON 瑙ｆ瀽澶辫触锛屼細鐢ㄥ叧閿瘝鍖归厤闄嶇骇鈥斺€旀棦涓嶅惈鍚﹀畾璇嶄篃涓嶅惈鑲畾璇嶆椂锛屼篃鍒や笉閫氳繃銆傞噸璇曟渶澶?2 娆★紝瓒呰繃鍚庝繚鐣欏綋鍓嶇粨鏋滐紝鑰屼笉鏄棤闄愬惊鐜€?|
| **Planner 鍜?Worker 鐨勫璇濆巻鍙蹭細琚帇缂╁悧锛?* | 浼氥€係ubAgent 鍐呴儴涔熸湁 `ConversationHistoryCompactor`锛屽湪 `execute()` 鏂规硶鐨勫惊鐜腑 `maybeCompactHistory()` 浼氬湪璋?LLM 鍓嶈瘎浼般€傛瘡涓?SubAgent 鏄嫭绔嬬殑 history 瀹炰緥锛屼簰鐩镐笉褰卞搷銆?|

---

## 3. 鎵嬪啓 MCP 鍗忚涓庡伐鍏风敓鎬侀泦鎴?
### 绠€鍘嗗師璇?
> 鍩轰簬 JSON-RPC 2.0 鎵嬪啓 MCP 瀹㈡埛绔紝瀹炵幇 stdio 瀛愯繘绋嬩笌 Streamable HTTP 鍙屼紶杈撻€氶亾锛涜璁″懡鍚嶇┖闂撮殧绂讳笌鐑敞鍐屾満鍒讹紝闆跺閮?SDK 渚濊禆鎺ュ叆 60+ 澶栭儴宸ュ叿銆?
### 浠ｇ爜瀹炵幇

#### JSON-RPC 2.0 鎵嬪啓 鈥?`JsonRpcClient`

**鏂囦欢**: `src/main/java/com/CodeCLI/mcp/jsonrpc/JsonRpcClient.java`

**鏍稿績瀹炵幇**: 鐢?Jackson 鐩存帴鏋勯€犲拰瑙ｆ瀽 JSON-RPC 鎶ユ枃锛岄浂澶栭儴 SDK 渚濊禆銆?
```java
// 璇锋眰鏋勯€?ObjectNode request = MAPPER.createObjectNode();
request.put("jsonrpc", "2.0");
request.put("id", id);  // AtomicLong 鑷 ID
request.put("method", method);
request.set("params", params);

// 璇锋眰-鍝嶅簲鍖归厤锛欳oncurrentHashMap<Long, CompletableFuture<JsonNode>>
private final ConcurrentHashMap<Long, CompletableFuture<JsonNode>> pending = ...;

// 鍙戦€佽姹?transport.send(request);
// 绛夊緟鍝嶅簲锛堝甫瓒呮椂锛?return future.get(timeoutSeconds + 1, TimeUnit.SECONDS);

// 鏀跺埌鍝嶅簲鏃舵寜 id 鏌ユ壘骞?complete
private void handleMessage(JsonNode message) {
    long id = idNode.asLong();
    CompletableFuture<JsonNode> future = pending.remove(id);
    // 鏃?id 鈫?瑙嗕负閫氱煡锛岄亶鍘?notificationListeners
    if (idNode == null || idNode.isNull()) {
        for (Consumer<JsonNode> listener : notificationListeners) listener.accept(message);
        return;
    }
    // 鏈?error 鈫?completeExceptionally
    // 鏈?result 鈫?complete
}
```

**瓒呮椂瀹炵幇**: `ScheduledExecutorService` 璋冨害寤惰繜浠诲姟锛岃秴鏃跺悗浠?`pending` 绉婚櫎骞?`completeExceptionally(TimeoutException)`銆?
#### 鍙屼紶杈撻€氶亾

**Stdio** 鈥?`StdioTransport`:

```java
// 鏂囦欢: src/main/java/com/CodeCLI/mcp/transport/StdioTransport.java
// 鍚姩瀛愯繘绋?ProcessBuilder builder = new ProcessBuilder(command, args...);
this.process = builder.start();

// 涓変釜 daemon 绾跨▼锛?// 1. stdout 璇伙細BufferedReader 鈫?JSON 瑙ｆ瀽 鈫?閫氱煡 listeners
// 2. stderr 璇伙細鐜舰缂撳啿鍖猴紝鏈€澶?200 琛?// 3. 鍐欙細BufferedWriter + OutputStreamWriter

// 鍏抽棴锛氬厛 stdin EOF 鈫?waitFor 1s 鈫?destroy 鈫?waitFor 2s 鈫?destroyForcibly
```

**Streamable HTTP** 鈥?`StreamableHttpTransport`:

```java
// 鏂囦欢: src/main/java/com/CodeCLI/mcp/transport/StreamableHttpTransport.java
// OkHttp POST 璇锋眰
RequestBody body = RequestBody.create(MAPPER.writeValueAsString(message), JSON);
Request.Builder builder = new Request.Builder()
    .url(url)
    .header("MCP-Protocol-Version", PROTOCOL_VERSION)
    .header("Mcp-Session-Id", sessionId)
    .post(body);

// 鍝嶅簲瑙ｆ瀽锛氭敮鎸?JSON 鍜?SSE 涓ょ鏍煎紡
// SSE 瑙ｆ瀽锛氭寜 data: 琛屽垎鍓诧紝绌鸿鍒嗛殧浜嬩欢
// 浼氳瘽绠＄悊锛歁cp-Session-Id header锛孌ELETE 璇锋眰鍏抽棴
```

**缁熶竴鎺ュ彛** 鈥?`McpTransport`:

```java
// 鏂囦欢: src/main/java/com/CodeCLI/mcp/transport/McpTransport.java
public interface McpTransport {
    void send(JsonNode message) throws IOException;
    void onReceive(Consumer<JsonNode> listener);
    List<String> stderrLines();
    Long processId();
    String transportName();
    void close();
}
```

#### 鍛藉悕绌洪棿闅旂

**鏂囦欢**: `McpToolDescriptor.java`

```java
public static String namespaced(String serverName, String toolName) {
    return "mcp__" + serverName + "__" + toolName;
}
```

杩欎釜鍓嶇紑鍦ㄥ涓湴鏂硅鐢ㄥ埌锛?
- **`ToolRegistry.replaceMcpToolOutputsForServer()`**: 鎸?`"mcp__" + serverName + "__"` 鍓嶇紑鎵归噺鏇挎崲鏌?server 鐨勬墍鏈夊伐鍏?- **`ApprovalPolicy.isMcpTool()`**: 鎸?`mcp__` 鍓嶇紑璇嗗埆 MCP 宸ュ叿锛岄粯璁ら渶 HITL 瀹℃壒
- **`AuditLog`**: 鎸?`mcp__` 鍓嶇紑鍔ㄦ€佺撼鍏ュ璁?- **`base.md` 宸ュ叿鍒楄〃**: 绗?13 涓伐鍏峰畾涔変负 `mcp__{server}__{tool}` 妯″紡

#### 鐑敞鍐屾満鍒?
**鏂囦欢**: `McpServerManager.java`, `registerNotificationHandlers()` 鏂规硶

```java
client.onNotification(router);
router.on("notifications/tools/list_changed", ignored -> {
    // 鑷姩閲嶆柊 listTools() 骞?replaceTools() 鍒?ToolRegistry
    List<McpToolDescriptor> tools = buildToolList(server, client);
    replaceTools(server, client, tools);
    server.tools(tools);
});
router.on("notifications/resources/list_changed", ignored -> resourceCache.invalidateServer(server.name()));
router.on("notifications/resources/updated", params -> {
    String uri = params.path("uri").asText("");
    if (!uri.isBlank()) resourceCache.invalidateResource(server.name(), uri);
});
```

**`replaceTools()` 鏂规硶**:

```java
private void replaceTools(McpServer server, McpClient client, List<McpToolDescriptor> tools) {
    toolRegistry.replaceMcpToolOutputsForServer(server.name(), tools,
        descriptor -> isResourceVirtualTool(descriptor)
            ? args -> ToolOutput.text(McpResourceTool.invoker(client, descriptor).apply(args))
            : args -> invokeMcpToolOutput(client, descriptor, args));
}
```

#### 闆跺閮?SDK 渚濊禆

鏁翠釜 MCP 妯″潡鍙緷璧栵細
- **Jackson**: JSON 搴忓垪鍖?鍙嶅簭鍒楀寲锛坄ObjectMapper`銆乣JsonNode`锛?- **OkHttp**: HTTP 浼犺緭锛坄OkHttpClient`銆乣Request`銆乣Response`锛?
娌℃湁寮曠敤浠讳綍 MCP 瀹樻柟 SDK锛坢odelcontextprotocol/java-sdk锛夋垨绗笁鏂?MCP 搴撱€傚崗璁彙鎵嬨€佸伐鍏峰彂鐜般€丼chema 娓呮礂銆侀€氱煡璺敱鍏ㄩ儴鑷瀹炵幇銆?
#### 鍗忚鎻℃墜 鈥?`McpClient.initialize()`

```java
// 鏂囦欢: McpClient.java
public void initialize() throws IOException {
    // 鍙戦€?initialize 璇锋眰
    JsonNode result = rpc.request("initialize", McpInitializeRequest.toJson(), timeoutSeconds);
    serverCapabilities = result.path("capabilities");
    // 鍙戦€?initialized 閫氱煡
    rpc.sendNotification("notifications/initialized", null);
}
```

**`McpInitializeRequest`** 鍖呭惈鍗忚鐗堟湰 `2025-03-26`銆佸鎴风鍚嶇О/鐗堟湰銆佽兘鍔涘０鏄庛€?
### 闈㈣瘯鍥炵瓟

> **闈㈣瘯瀹?*: 浣犵畝鍘嗛噷鍐?鎵嬪啓 MCP 鍗忚"锛屼负浠€涔堜笉鐢ㄥ畼鏂?SDK锛?
**鏍囧噯鍥炵瓟**:

"MCP 鍗忚鏈韩闈炲父绠€鍗曗€斺€斿熀浜?JSON-RPC 2.0锛屾牳蹇冨彧鏈?initialize銆乴istTools銆乧allTool銆乴istResources銆乺eadResource 鍑犱釜鏂规硶銆傚畼鏂?SDK 浼氬鍔犻澶栫殑鎶借薄灞傚拰渚濊禆锛岃€屾垜浠瘎浼板悗璁や负锛?
1. **鍗忚瓒冲绠€鍗?*: 鑷繁鐢?Jackson 鏋勯€?JSON-RPC 鎶ユ枃锛屽嚑鍗佽浠ｇ爜灏辫兘瀹炵幇瀹屾暣鐨勮姹?鍝嶅簲鍖归厤 + 瓒呮椂 + 閫氱煡璺敱銆俙ConcurrentHashMap<Long, CompletableFuture<JsonNode>>` 灏辨槸鏍稿績鏁版嵁缁撴瀯銆?
2. **閬垮厤渚濊禆鑶ㄨ儉**: 寮曞叆瀹樻柟 SDK 浼氬甫鏉ラ澶栫殑渚濊禆閾惧拰鐗堟湰鍏煎闂銆傞浂澶栭儴 SDK 渚濊禆璁╂暣涓?MCP 妯″潡鍙緷璧?Jackson 鍜?OkHttp锛岀淮鎶ゆ垚鏈瀬浣庛€?
3. **鐏垫椿鎬?*: 鑷繁瀹炵幇鍙互绮剧‘鎺у埗姣忎釜缁嗚妭鈥斺€旀瘮濡?`McpSchemaSanitizer` 娓呮礂宸ュ叿 `inputSchema`锛堝幓鎺?`$schema`/`$id`/`$ref` 寮曠敤锛屾妸 `anyOf`/`oneOf` 鑱斿悎绫诲瀷鎷嶅钩鎴?description锛宒escription 鎴柇鍒?1000 瀛楃锛岀‘淇?schema 鏈?`type`+`properties`锛夛紝`McpResourceTool` 鎶?Resource 鎿嶄綔鍖呰鎴?`list_resources`/`read_resource` 涓や釜铏氭嫙宸ュ叿锛宍NotificationRouter` 鎶?server鈫抍lient 鐨勯€氱煡璺敱鍒?handler 骞跺湪鐙珛绾跨▼寮傛娲惧彂锛堥伩鍏嶅湪 transport reader 绾跨▼閲屽悓姝ユ墽琛?handler 鍙戣姹傘€佽嚜宸辩瓑鑷繁鐨勫搷搴旈€犳垚姝婚攣锛夈€傝繖浜涘畾鍒跺湪瀹樻柟 SDK 閲屽彲鑳介渶瑕?hack 鎴?fork銆?
4. **鍙屼紶杈撻€氶亾**: 瀹樻柟鐨?Transport 瀹炵幇涓嶄竴瀹氬悓鏃惰鐩?stdio 鍜?Streamable HTTP锛岃€屾垜浠殑 `McpTransport` 鎺ュ彛缁熶竴浜嗕袱绉嶄紶杈擄紝涓婂眰瀹屽叏鏃犳劅鐭ャ€?

> **闈㈣瘯瀹?*: 鍛藉悕绌洪棿闅旂鎬庝箞鍋氱殑锛?
**鏍囧噯鍥炵瓟**:

"姣忎釜 MCP 宸ュ叿娉ㄥ唽鏃剁敓鎴?`namespacedName`锛屾牸寮忎负 `mcp__{serverName}__{toolName}`銆傝繖涓懡鍚嶈鍒欒疮绌挎暣涓郴缁燂細

- 鍦?`ToolRegistry` 涓寜 `mcp__{serverName}__` 鍓嶇紑鎵归噺鏇挎崲鏌?server 鐨勬墍鏈夊伐鍏封€斺€斿綋 server 鍥?`tools/list_changed` 閫氱煡鏇存柊宸ュ叿鍒楄〃鏃讹紝鏃у伐鍏疯鍓嶇紑鍖归厤鍒犻櫎锛屾柊宸ュ叿娉ㄥ唽銆?- 鍦?`ApprovalPolicy` 涓€氳繃 `mcp__` 鍓嶇紑璇嗗埆 MCP 宸ュ叿锛岄粯璁ら渶瑕?HITL 瀹℃壒锛岄伩鍏?LLM 鏈粡鐢ㄦ埛纭灏辫皟鐢ㄥ閮ㄥ伐鍏枫€?- 鍦?`AuditLog` 涓姩鎬佺撼鍏ユ墍鏈?`mcp__` 鍓嶇紑宸ュ叿鐨勫璁°€?
杩欐牱鍚屼竴涓?LLM 鐨?tool_choice 绌洪棿閲岋紝`mcp__filesystem__read_file` 鍜?`mcp__git__read_file` 涓嶄細鍐茬獊锛屽洜涓哄墠缂€涓嶅悓銆?

### 鍙兘鐨勮拷闂?
| 杩介棶 | 寤鸿鍥炵瓟 |
|------|---------|
| **JSON-RPC 鐨勮姹傚拰鍝嶅簲鎬庝箞鍖归厤鐨勶紵** | 姣忎釜璇锋眰鍒嗛厤涓€涓?`AtomicLong` 鑷 ID锛岃姹傚彂閫佸墠鎶?`CompletableFuture` 鏀惧叆 `ConcurrentHashMap<Long, CompletableFuture<JsonNode>>`銆傛敹鍒板搷搴旀椂鎸?ID 鏌ユ壘骞?`complete()`銆傛病鏈?ID 鐨勬秷鎭涓洪€氱煡锛岄亶鍘?`notificationListeners`銆?|
| **MCP 宸ュ叿鐨?Schema 鎬庝箞娓呮礂锛?* | `McpSchemaSanitizer.sanitize()` 閫掑綊閬嶅巻 schema锛氬垹闄?`$schema`/`$id`/`$ref` 鍏冨瓧娈碉紙闃叉 LLM 鐪嬪埌鏃犳硶瑙ｆ瀽鐨勫紩鐢級锛屾妸 `anyOf`/`oneOf` 鑱斿悎绫诲瀷鍘嬪钩鎴?`type: object` + description锛坄"anyOf options: string, integer"`锛夛紝description 鎴柇鍒?1000 瀛楃锛屾渶缁堜繚璇?schema 涓€瀹氭湁 `type` 鍜?`properties`锛屽惁鍒?fallback 鎴愮┖瀵硅薄 schema銆?|
| **Server 鍚姩澶辫触浼氶樆濉炴暣涓?CLI 鍚楋紵** | 涓嶄細銆俙McpServerManager.startAll()` 涓烘瘡涓?server 鍚姩鐙珛绾跨▼锛屽崟涓?server 澶辫触鍙爣 ERROR 鐘舵€侊紝鍏朵粬 server 缁х画銆俙/mcp` 鍛戒护鍙互鏌ョ湅鎵€鏈?server 鐨勭姸鎬併€傞厤缃姞杞芥椂锛宍${VAR}` 鐜鍙橀噺鏇挎崲涔熷仛浜嗗紓甯镐繚鎶わ紝鍗曚釜 server 閰嶇疆閿欒涓嶉樆濉炲叾浠?server銆?|
| **鎬庝箞澶勭悊 MCP 宸ュ叿鐨勮秴鏃讹紵** | `JsonRpcClient.request()` 璋冪敤浜?`future.get(timeoutSeconds + 1, TimeUnit.SECONDS)`锛岃秴鏃跺悗 `pending.remove(id)` 骞舵姏 `IOException`銆俙McpServerManager` 鐨?`invokeMcpToolOutput()` 浼?catch 寮傚父骞惰繑鍥?`ToolOutput.text("MCP 宸ュ叿璋冪敤澶辫触: ...")`锛屼笉浼氳鏁翠釜 Agent 寰幆宕╂簝銆?|
| **Streamable HTTP 鐨?SSE 鎬庝箞瑙ｆ瀽鐨勶紵** | `StreamableHttpTransport` 鏀跺埌 HTTP 鍝嶅簲鍚庯紝鍏堟鏌?Content-Type 澶淬€傚鏋滄槸 `text/event-stream`锛屾寜 `data:` 琛岃В鏋?SSE 娴侊紝绌鸿鍒嗛殧浜嬩欢銆傞潪 SSE 鍝嶅簲鐩存帴浣滀负 JSON 瑙ｆ瀽銆俙parseSse()` 鏂规硶瀹炵幇杩欎竴閫昏緫銆?|

---

## 4. Prompt 鍒嗗眰鏋舵瀯涓?KV Cache 浼樺寲

### 绠€鍘嗗師璇?
> 灏嗙郴缁熸彁绀鸿瘝浠庣‖缂栫爜閲嶆瀯涓哄垎灞?Markdown 鏂囦欢锛屾寜"绋冲畾鍦ㄥ墠銆佸姩鎬佸湪鍚?缁勮浠ユ渶澶у寲 KV cache 鍛戒腑鐜囷紝鏀寔涓夌骇瑕嗙洊闆朵唬鐮佸畾鍒?Agent 琛屼负銆?
### 浠ｇ爜瀹炵幇

#### 浠庣‖缂栫爜鍒板垎灞?Markdown 鈥?`PromptAssembler` + `PromptRepository`

**鏂囦欢**: `src/main/java/com/CodeCLI/prompt/PromptAssembler.java`

**缁勮椤哄簭** (`assemble()` 鏂规硶):

```java
public String assemble(PromptMode mode, PromptContext context) {
    // 1. base.md 鈥?鏋佺ǔ瀹氾細韬唤銆佽瑷€銆佸伐鍏峰畾涔夈€佺瓥鐣?    append(prompt, repository.loadRequired("base.md"));

    // 2. personalities/calm.md 鈥?鏋佺ǔ瀹氾細鎬ф牸璁惧畾
    append(prompt, repository.loadRequired("personalities/calm.md"));

    // 3. modes/{mode}.md 鈥?绋冲畾锛氭ā寮忔寚浠わ紙agent/plan/team-*锛?    append(prompt, applyVariables(repository.loadRequired(mode.resourcePath()), ctx));

    // 4. approvals/{mode}.md 鈥?绋冲畾锛氬鎵规ā寮忥紙auto/suggest/never锛?    append(prompt, repository.loadRequired("approvals/" + approvalMode(ctx) + ".md"));

    // 5. Runtime Context 鈥?鍗婂姩鎬侊細鏃ユ湡/鏃跺尯锛岃法澶╂墠鍙?    append(prompt, runtimeContext());

    // 6. Project Context 鈥?鍔ㄦ€侊細PAI.md + 璁板繂妫€绱?+ MCP resources
    append(prompt, dynamicSection("Project Context", ctx.projectMemoryContext(), ...));

    // 7. Skills 鈥?鍔ㄦ€侊細Skill 绱㈠紩
    append(prompt, dynamicSection("Skills", ctx.skillIndex()));

    // 8. context-management.md 鈥?鏋佺ǔ瀹氾細涓婁笅鏂囩鐞嗘彁绀?    append(prompt, repository.loadRequired("context/context-management.md"));

    // 9. handoff.md 鈥?鏋佺ǔ瀹氾細浜ゆ帴鎻愮ず
    append(prompt, repository.loadRequired("handoff.md"));

    return prompt.toString().trim();
}
```

#### 涓夌骇瑕嗙洊 鈥?`PromptRepository`

**鏂囦欢**: `src/main/java/com/CodeCLI/prompt/PromptRepository.java`

```java
public String loadRequired(String relativePath) {
    // 1. 鍔犺浇 JAR 鍐呯疆锛坈lasspath:prompts/锛?    String content = loadBuiltin(normalized);

    // 2. 鐢ㄦ埛绾ц鐩栵紙~/.CodeCLI/prompts/锛?    content = overrideIfPresent(userPromptsDir, normalized, content);

    // 3. 椤圭洰绾ц鐩栵紙.CodeCLI/prompts/锛?    content = overrideIfPresent(projectPromptsDir, normalized, content);

    return content.trim();
}
```

**13 涓?Markdown 妯℃澘鏂囦欢**:

```
src/main/resources/prompts/
鈹溾攢鈹€ base.md                              # 鏍稿績锛氳韩浠姐€佽瑷€銆佸伐鍏枫€佺瓥鐣?鈹溾攢鈹€ handoff.md                           # 浜ゆ帴鎻愮ず
鈹溾攢鈹€ personalities/
鈹?  鈹斺攢鈹€ calm.md                          # 鎬ф牸璁惧畾
鈹溾攢鈹€ modes/
鈹?  鈹溾攢鈹€ agent.md                         # 榛樿 Agent 妯″紡
鈹?  鈹溾攢鈹€ plan.md                          # Plan-and-Execute
鈹?  鈹溾攢鈹€ team-planner.md                  # Multi-Agent Planner
鈹?  鈹溾攢鈹€ team-worker.md                   # Multi-Agent Worker
鈹?  鈹斺攢鈹€ team-reviewer.md                 # Multi-Agent Reviewer
鈹溾攢鈹€ approvals/
鈹?  鈹溾攢鈹€ auto.md                          # 鑷姩鎵瑰噯
鈹?  鈹溾攢鈹€ suggest.md                       # 寤鸿妯″紡
鈹?  鈹斺攢鈹€ never.md                         # 姘镐笉鎵瑰噯
鈹斺攢鈹€ context/
    鈹斺攢鈹€ context-management.md            # 涓婁笅鏂囩鐞嗘彁绀?```

#### KV Cache 鍛戒腑鐜囦紭鍖栧師鐞?
**"绋冲畾鍦ㄥ墠銆佸姩鎬佸湪鍚?鐨勬帓鍒?*:

| 椤哄簭 | 娈?| 绋冲畾鎬?| 鍙樻洿棰戠巼 |
|------|----|--------|---------|
| 1 | `base.md` | 鏋佺ǔ瀹?| 璺ㄧ増鏈笉鍙?|
| 2 | `personalities/calm.md` | 鏋佺ǔ瀹?| 璺ㄧ増鏈笉鍙?|
| 3 | `modes/{mode}.md` | 绋冲畾 | 鍒囨崲妯″紡鎵嶅彉 |
| 4 | `approvals/{mode}.md` | 绋冲畾 | 鍒囨崲瀹℃壒妯″紡鎵嶅彉 |
| 5 | Runtime Context | 鍗婂姩鎬?| 璺ㄥぉ鎵嶅彉 |
| 6 | Project Context | 鍔ㄦ€?| 姣忚疆鍙兘鍙?|
| 7 | Skills | 鍔ㄦ€?| 姣忚疆鍙兘鍙?|
| 8 | `context-management.md` | 鏋佺ǔ瀹?| 璺ㄧ増鏈笉鍙?|
| 9 | `handoff.md` | 鏋佺ǔ瀹?| 璺ㄧ増鏈笉鍙?|

**鏁堟灉**: 鍓?4 娈碉紙绾?70% 鐨?system prompt 鍐呭锛夊湪浼氳瘽鏈熼棿瀹屽叏涓嶅彉锛屾瀯鎴?KV cache 鐨勭ǔ瀹氬墠缂€銆傜 6-7 娈佃櫧鐒舵瘡杞彲鑳藉彉锛屼絾瀹冧滑鍦?prompt 鏈熬锛屼笉褰卞搷鍓嶇紑鐨?cache 鍛戒腑銆?
**LLM 渚ф敮鎸?*: 鍚勫ぇ provider 閮芥敮鎸?prefix caching銆俙LlmClient` 鎺ュ彛瀹氫箟浜?`promptCacheMode()` 鏂规硶锛?
| Provider | `promptCacheMode()` | 缂撳瓨鏈哄埗 |
|----------|---------------------|----------|
| GLM | `glm-prompt-cache` | GLM 骞冲彴绾?prompt cache |
| DeepSeek | `automatic-prefix-cache` | DeepSeek 鑷姩鍓嶇紑缂撳瓨 |
| Kimi | `moonshot-context-cache` | Moonshot 涓婁笅鏂囩紦瀛?|
| Step | `step-prefix-cache` | StepFun 鍓嶇紑缂撳瓨 |

`AgentBudget.recordTokens()` 绱 `cachedInputTokens`锛宍TokenUsageFormatter.estimatedCostCny()` 鍦ㄨ绠楁垚鏈椂瀵圭紦瀛?token 浣跨敤鏇翠綆鐨勪环鏍笺€?
#### 妯℃澘鍙橀噺鏇挎崲 鈥?`applyVariables()`

```java
private String applyVariables(String prompt, PromptContext context) {
    String result = prompt;
    for (Map.Entry<String, String> entry : context.variables().entrySet()) {
        result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
    }
    result = result.replace("{{taskType}}", context.variable("taskType"));
    result = result.replace("{{taskDescription}}", context.variable("taskDescription"));
    return result;
}
```

### 闈㈣瘯鍥炵瓟

> **闈㈣瘯瀹?*: 浣犵畝鍘嗛噷鍐?Prompt 鍒嗗眰鏋舵瀯"锛屽叿浣撴€庝箞鍒嗗眰鐨勶紵

**鏍囧噯鍥炵瓟**:

"鏃х増 system prompt 鐩存帴鍐欏湪 `Agent.java` 鐨勫瓧绗︿覆鎷兼帴涓紝姣忔淇敼閮介渶瑕侀噸鏂扮紪璇戙€傞噸鏋勫悗锛屾垜鍋氫簡涓変欢浜嬶細

**1. 鍒嗗眰 Markdown 妯℃澘**

灏?system prompt 鎷嗘垚 13 涓嫭绔嬬殑 Markdown 鏂囦欢锛屾寜鍔熻兘鍒嗕负锛氭牳蹇冭韩浠斤紙base.md锛夈€佹€ф牸锛坧ersonalities/锛夈€佹ā寮忔寚浠わ紙modes/锛夈€佸鎵规ā寮忥紙approvals/锛夈€佷笂涓嬫枃绠＄悊锛坈ontext/锛夈€佷氦鎺ユ彁绀猴紙handoff.md锛夈€傛瘡涓枃浠跺彧鍏虫敞涓€涓亴璐ｃ€?
**2. 涓夌骇瑕嗙洊鏈哄埗**

`PromptRepository` 浠庝笁涓綅缃垎灞傚姞杞斤細
- JAR 鍐呯疆锛坄classpath:prompts/`锛夛細闅忕増鏈彂甯?- 鐢ㄦ埛绾э紙`~/.CodeCLI/prompts/`锛夛細鐢ㄦ埛鍏ㄥ眬鑷畾涔?- 椤圭洰绾э紙`.CodeCLI/prompts/`锛夛細椤圭洰鐗瑰畾瑕嗙洊

鐢ㄦ埛鎴栭」鐩彧闇€瑕佹斁鍚屽悕鏂囦欢灏辫兘瑕嗙洊鍐呯疆妯℃澘锛屼笉闇€瑕佹敼浠ｇ爜銆佷笉闇€瑕侀噸鏂扮紪璇戙€傛瘮濡傚洟闃熷彲浠ヨ嚜瀹氫箟 `base.md` 涓殑宸ュ叿绛栫暐锛屾斁鍦ㄩ」鐩洰褰曚笅鐨?`.CodeCLI/prompts/` 鍗冲彲銆?
**3. 绋冲畾鍦ㄥ墠銆佸姩鎬佸湪鍚?*

`PromptAssembler.assemble()` 鎸夌壒瀹氶『搴忔嫾鎺ワ細绋冲畾娈碉紙base + personality + mode + approval锛夋帓鍦ㄦ渶鍓嶉潰锛屽姩鎬佹锛圥roject Context + Skills锛夋帓鍦ㄤ腑闂达紝鏈€鍚庡張鏄ǔ瀹氭锛坈ontext-management + handoff锛夈€?
杩欑鎺掑垪纭繚锛氬墠 70% 鐨?system prompt 鍦ㄦ暣涓細璇濇湡闂村畬鍏ㄤ笉鍙橈紝鍚勫ぇ LLM provider 鐨勫墠缂€缂撳瓨锛坧refix caching锛夊彲浠ュ懡涓€傚彧鏈夋湯灏剧殑 Project Context 鍜?Skills 娈垫瘡杞渶瑕侀噸鏂拌绠椼€傚杞璇濅腑锛岄杞箣鍚庣殑鎺ㄧ悊鍙渶瑕佽绠楀姩鎬佹鐨?KV锛屾垚鏈樉钁楅檷浣庛€?

### 鍙兘鐨勮拷闂?
| 杩介棶 | 寤鸿鍥炵瓟 |
|------|---------|
| **KV Cache 鍛戒腑鐜囪兘鎻愬崌澶氬皯锛熸湁鏁版嵁鍚楋紵** | 鍏蜂綋鏁板€煎彇鍐充簬 provider 鐨勫疄鐜般€備粠鍘熺悊涓婂垎鏋愶細鏃х増 system prompt 鎶婅蹇嗘绱㈢粨鏋滄贩鍦ㄧǔ瀹氬唴瀹逛腑闂达紝姣忔妫€绱㈢粨鏋滃彉鍖栭兘瀵艰嚧鏁翠釜鍓嶇紑鐨?cache 澶辨晥銆傞噸鏋勫悗锛岀ǔ瀹氭鍏ㄩ儴鍓嶇疆锛屽彧瑕佺ǔ瀹氭鍐呭涓嶅彉灏辫兘鍛戒腑 cache銆傚湪 DeepSeek 鐨勮嚜鍔ㄥ墠缂€缂撳瓨涓紝瀹炴祴 system prompt 鐨?cache 鍛戒腑鐜囦粠鎺ヨ繎 0% 鎻愬崌鍒扮ǔ瀹氬墠缂€閮ㄥ垎鐨?100% 鍛戒腑銆?|
| **涓夌骇瑕嗙洊鐨勪紭鍏堢骇鎬庝箞澶勭悊锛?* | 浼樺厛绾э細椤圭洰绾?> 鐢ㄦ埛绾?> JAR 鍐呯疆銆俙overrideIfPresent()` 鏂规硶鍏堝姞杞藉唴缃紝鐒跺悗鐢ㄧ敤鎴风骇瑕嗙洊锛屾渶鍚庣敤椤圭洰绾ц鐩栥€傚鏋滈」鐩骇鏂囦欢瀛樺湪锛屽氨鐢ㄥ畠锛涘鏋滀笉瀛樺湪锛岀敤鐢ㄦ埛绾э紱濡傛灉鐢ㄦ埛绾т篃涓嶅瓨鍦紝鐢ㄥ唴缃€?|
| **濡傛灉鏌愪釜妯℃澘鏂囦欢涓嶅瓨鍦ㄤ細鎬庢牱锛?* | `loadRequired()` 鏂规硶鍦ㄦ枃浠剁己澶辨椂鎶?`IllegalStateException`锛岄伩鍏嶉潤榛樹娇鐢ㄧ┖ prompt銆備絾濡傛灉浣犲彧鎯宠鐩栭儴鍒嗘ā鏉匡紝鍙渶鏀惧搴旂殑鏂囦欢鈥斺€斾笉瀛樺湪鐨勬枃浠朵笉浼氳瑕嗙洊銆?|
| **`PromptAssembler` 鎬庝箞澶勭悊涓嶅悓妯″紡鐨?prompt锛?* | `PromptMode` 鏋氫妇瀹氫箟浜嗗嚑绉嶆ā寮忥細`AGENT`銆乣PLAN`銆乣TEAM_PLANNER`銆乣TEAM_WORKER`銆乣TEAM_REVIEWER`銆傛瘡绉嶆ā寮忓搴?`prompts/modes/{mode}.md`銆俙PromptAssembler.assemble(mode, context)` 鏍规嵁 mode 鍔犺浇瀵瑰簲鐨勬ā寮忔寚浠ゆ枃浠躲€?|
| **Runtime Context 鍖呭惈浠€涔堬紵** | 褰撳墠鏃ユ湡銆佹椂鍖恒€傝繖浜涗俊鎭法澶╂墠鍙橈紝鎵€浠ユ槸"鍗婂姩鎬?銆俙runtimeContext()` 鏂规硶鐢?`LocalDate.now()` 鍜?`ZoneId.systemDefault()` 鐢熸垚銆?|
| **Project Context 鍖呭惈浠€涔堬紵** | 涓夐儴鍒嗭細`projectMemoryContext`锛圥AI.md 椤圭洰璁板繂鏂囦欢锛夈€乣memoryContext`锛堜粠闀挎湡璁板繂妫€绱㈠埌鐨勭浉鍏充簨瀹烇級銆乣externalContext`锛圡CP Resource 绱㈠紩锛夈€俙PromptContext` 鐢?Builder 妯″紡缁勮杩欎簺鍔ㄦ€佸唴瀹广€?|

---

## 5. MCP 宸ュ叿瀹炰緥璁茶В锛氭帴鍏ヤ簡鍝簺 + chrome-devtools 鍏ㄦ祦绋?
### 5.1 鍏堝洖绛?鎺ュ叆浜嗗摢浜?MCP 宸ュ叿"

闈㈣瘯瀹橀棶杩欎釜锛?*鍏堣鎬婚噺鍐嶅垎绫?*锛岃瘉鏄庢槸鐢熸€佺骇鎺ュ叆鑰屼笉鏄帺鍏?demo锛?
> "CodeCLI 鍚姩鏃跺苟鍙戞媺璧峰涓?MCP server锛屽惎鍔ㄩ〉浼氭樉绀?`MCP 4/4 路 61 tools`鈥斺€? 涓?server 鍏?61 涓閮ㄥ伐鍏枫€傚垎涓ょ被锛?>
> **stdio 鏈湴 server**锛堝瓙杩涚▼锛夛細
> - `chrome-devtools`锛欸oogle 瀹樻柟 `chrome-devtools-mcp@latest`锛屾彁渚?`navigate_page` / `take_snapshot` / `click` / `fill_form` / `list_console_messages` / `list_network_requests` 绛?30+ 娴忚鍣ㄨ嚜鍔ㄥ寲宸ュ叿锛屽鐞?SPA銆丣S 娓叉煋銆侀槻鐖銆佽〃鍗曚氦浜?> - `fetch`锛歚mcp-server-fetch`锛岀綉椤垫姄鍙?> - `git`锛歚mcp-server-git`锛実it 浠撳簱鎿嶄綔锛宍--repository ${PROJECT_DIR}` 缁戝畾褰撳墠椤圭洰
>
> **杩滅▼ Streamable HTTP server**锛?> - `step_search`锛歋tepFun 鐨勮繙绋?web_search MCP锛坄https://api.stepfun.com/step_plan/v1/mcp/web_search/mcp`锛夛紝妫€娴嬪埌 `STEP_API_KEY` 鏃惰嚜鍔ㄥ唴缃紝鏃犻渶鎵嬪啓閰嶇疆
>
> 闄や簡閰嶇疆閲岀殑 server锛屽彧瑕?server 澹版槑浜?`resources` capability锛岃繕浼氳嚜鍔ㄦ敞鍐?`mcp__{server}__list_resources` / `mcp__{server}__read_resource` 涓や釜铏氭嫙宸ュ叿銆?
>

鐒跺悗鎸?**chrome-devtools** 璁插畬鏁存祦绋嬧€斺€斿畠鏄粯璁ゅ唴缃€佹晠浜嬫渶瀹屾暣锛堟湁"鎺ュ叆鈫掍娇鐢ㄢ啋闄嶇骇鈫掑璁?鍏ㄩ摼璺級銆?
### 5.2 鎺ュ叆闃舵锛堝惎鍔ㄦ椂锛岃浠ｇ爜璺緞锛?
> **閰嶇疆鍔犺浇涓庡悎骞?*锛氬惎鍔ㄦ椂 `McpConfigLoader.load()` 璇讳袱涓枃浠垛€斺€旂敤鎴风骇 `~/.CodeCLI/mcp.json` + 椤圭洰绾?`.CodeCLI/mcp.json`锛岄」鐩骇鎸?server 鍚嶈鐩栫敤鎴风骇銆俙~/.CodeCLI/mcp.json` 涓嶅瓨鍦ㄦ椂鑷姩鍒涘缓榛樿 chrome-devtools 閰嶇疆锛堥浂閰嶇疆涓婃墜锛夛細
>
> ```json
> { "mcpServers": {
>     "chrome-devtools": {
>       "command": "npx",
>       "args": ["-y", "chrome-devtools-mcp@latest", "--isolated=true"]
>     }
> } }
> ```
>
> **`${VAR}` 灞曞紑**锛歚McpConfigLoader.expandString()` 鐢ㄦ鍒欏尮閰?`${VAR}`锛屾寜銆岀郴缁熺幆澧冨彉閲?鈫?绯荤粺灞炴€?鈫?椤圭洰 `.env` 鈫?鐢ㄦ埛 `~/.env`銆嶅洓绾ф煡鎵撅紝`${PROJECT_DIR}` / `${HOME}` 鏄唴缃彉閲忋€?*鏍￠獙**淇濊瘉姣忎釜 server 蹇呴』涓斿彧鑳介厤缃?`command`锛坰tdio锛夋垨 `url`锛圚TTP锛夈€?>
> **鍚姩 server**锛歚McpServerManager.startAll()` 涓烘瘡涓?server 璧风嫭绔嬬嚎绋嬪苟鍙戝惎鍔紝鍗曚釜 server 澶辫触鍙爣 ERROR 鐘舵€併€佷笉闃诲鍏朵粬 server銆傚 chrome-devtools锛?> - `StdioTransport` 鐢?`ProcessBuilder` 鎷夎捣 `npx -y chrome-devtools-mcp@latest --isolated=true` 瀛愯繘绋?> - `McpClient.initialize()` 璧?JSON-RPC `initialize` 鎻℃墜锛堝崗璁増鏈?`2025-03-26`锛夛紝淇濆瓨 `capabilities`锛屽啀鍙?`notifications/initialized`
>
> **宸ュ叿娉ㄥ唽**锛歚listTools()` 鎷垮埌 Chrome DevTools 鐨?30+ 涓伐鍏锋弿杩帮紝`McpSchemaSanitizer` 娓呮礂姣忎釜 `inputSchema`锛堝幓 `$schema`/`$id`/`$ref`銆乣anyOf`/`oneOf` 鑱斿悎绫诲瀷鎷嶅钩鎴?description銆佽秴闀?description 鎴柇锛夛紝鐒跺悗娉ㄥ唽鎴?**`mcp__chrome-devtools__navigate_page`銆乣mcp__chrome-devtools__take_snapshot`** 绛夊悕瀛楀啓杩?`ToolRegistry`锛屽悓鏃舵敞鍐?`tools/list_changed` 閫氱煡璺敱瀹炵幇鐑洿鏂般€俿erver 鐘舵€佺疆 `READY`銆?
### 5.3 浣跨敤闃舵锛圓gent 璋冪敤锛岃绔埌绔満鏅級

> **鍦烘櫙**锛氱敤鎴疯"甯垜鐪嬩笅杩欎釜鍏紬鍙锋枃绔犺浜嗕粈涔?锛圧EADME 閲岀殑鐪熷疄绀轰緥锛夈€?>
> 1. 鍐呯疆 `web_fetch` 鍏堣瘯鈥斺€斿井淇℃枃绔犳槸 JS 娓叉煋鐨?SPA锛岃繑鍥炵┖姝ｆ枃 + 杈圭晫鎻愮ず銆俙base.md` 鐨?Browser Policy 瑙勫畾锛歚web_fetch` 鎷垮埌绌烘鏂囨椂鑷姩 fallback 鍒版祻瑙堝櫒 MCP銆?> 2. ReAct 寰幆閲?Agent 鍙戣捣 `mcp__chrome-devtools__navigate_page` 宸ュ叿璋冪敤锛屽弬鏁?JSON 搴忓垪鍖栥€?> 3. `ApprovalPolicy.isMcpTool()` 璇嗗埆 `mcp__` 鍓嶇紑 鈫?**榛樿瑙﹀彂 HITL 瀹℃壒**锛堢敤鎴锋寜 `y` 鏀捐锛汬ITL 鐨?鍏ㄩ儴鏀捐"鎸?server 缁村害鐢熸晥锛岃繛缁祻瑙堝櫒鎿嶄綔瀵?`chrome-devtools` 涓€娆＄‘璁ゅ嵆鍙級銆?> 4. 瀹℃壒閫氳繃 鈫?`ToolRegistry.executeTool()` 鍒嗗彂鍒版敞鍐岀殑 invoker 鈫?`McpClient.callToolOutput()` 鈫?`JsonRpcClient.request("tools/call", ...)` 鈫?`StdioTransport.send()` 鍐欏瓙杩涚▼ stdin銆?> 5. Chrome DevTools 鎵ц娴忚鍣ㄦ搷浣滐紝JSON-RPC 鍝嶅簲浠?stdout 璇荤嚎绋嬪埌杈撅紝`JsonRpcClient.handleMessage()` 鎸?`id` 浠?`pending` Map 鍖归厤鍑?Future 骞?complete銆?> 6. 缁撴灉鍖呰鎴?`ToolOutput` 鍥炵亴 conversationHistory锛堝鏋滄槸鎴浘锛宍image` content 淇濈暀 base64 + mimeType 浣滀负澶氭ā鎬?image 鍧椾紶鍥炴ā鍨嬶級銆?> 7. **瀹¤**锛歚AuditLog` 璁板綍杩欐澶栭儴宸ュ叿璋冪敤锛宍token` / `key` / `password` / `Authorization` / `Bearer` 鍙傛暟鑷姩鑴辨晱銆?> 8. Agent 缁х画寰幆锛歚take_snapshot` 璇婚〉闈㈢粨鏋?鈫?閬囧埌鐧诲綍椤佃皟 `browser_connect` 鍒囨崲 shared 妯″紡澶嶇敤鐧诲綍鎬?鈫?`fill_form` 濉〃 鈫?`click` 鐐瑰嚮鈥︹€?> 9. 鑻ュ伐鍏疯皟鐢ㄥ紓甯革紝`invokeMcpToolOutput()` 鎹曡幏鍚庤繑鍥?`ToolOutput.text("MCP 宸ュ叿璋冪敤澶辫触...")`锛孉gent 鐪嬪埌澶辫触鑳芥崲绛栫暐锛?*涓嶄細璁╂暣涓惊鐜穿婧?*銆?
### 5.4 琚拷闂椂鑳芥嫈楂樼殑璇濇湳

| 杩介棶 | 鍥炵瓟瑕佺偣 |
|------|---------|
| "涓轰粈涔堟祻瑙堝櫒杩欏潡涓嶇敤 Playwright 鑷繁鍐欙紝鑰岀敤 MCP锛? | 澶嶇敤瀹樻柟缁存姢鐨?chrome-devtools-mcp锛?0+ 涓祻瑙堝櫒鎿嶄綔闆舵垚鏈幏寰楋紝杩樺ぉ鐒惰蛋缁熶竴鐨勫伐鍏锋敞鍐?HITL/瀹¤閾捐矾锛汳CP 鏄兘鍔涚敓鎬佺殑鏍囧噯鎺ュ彛锛屾崲 server 涓嶅奖鍝嶄笂灞?Agent銆?|
| "chrome-devtools 鍜屽唴缃?`web_fetch` 鐨勫垎宸ワ紵" | `web_fetch` 杞婚噺銆佽蛋闈欐€?SSR 椤甸潰锛汼PA/JS 娓叉煋/闃茬埇澧?闇€鐧诲綍鎬佹椂鎵嶈蛋娴忚鍣?MCP銆備袱鏉¤矾鍦?`base.md` 鐨?Browser Policy 閲屾槑纭啓姝伙紝`web_fetch` 绌烘鏂囪嚜鍔?fallback銆?|
| "61 涓伐鍏蜂細涓嶄細鎶婁笂涓嬫枃鎾戠垎锛? | 宸ュ叿鎻忚堪璧?tool definitions 浼犵粰 LLM锛屽彧浼?name/description/schema锛汳CP resources 鐨勬鏂?*涓嶈嚜鍔ㄦ敞鍏?*锛屽彧鍦ㄩ暱涓婁笅鏂囨ā寮忥紙鈮?2k锛夋敞鍏?URI/鎻忚堪绱㈠紩锛岄渶瑕佹椂鎸?`@server:uri` 鏄惧紡璇诲彇銆?|
| "`step_search` 杩滅▼ server 鎬庝箞閴存潈锛? | `Authorization: Bearer ${STEP_API_KEY}` header锛宍${STEP_API_KEY}` 鍦ㄥ惎鍔ㄦ椂浠庣幆澧冨彉閲?`.env` 灞曞紑銆傛娴嬪埌鍙橀噺灏辫嚜鍔ㄥ唴缃紝鏄惧紡鍚屽悕閰嶇疆鍙鐩栭粯璁ゅ湴鍧€銆?|
| "MCP 宸ュ叿璋冪敤澶辫触浼氭€庢牱锛? | 涓ゅ眰鍏滃簳锛歚JsonRpcClient` 灞傝秴鏃舵姏寮傚父锛沗McpServerManager.invokeMcpToolOutput()` 鎹曡幏鍚庤浆鎴?`ToolOutput.text("MCP 宸ュ叿璋冪敤澶辫触...")`锛屽伐鍏峰け璐ヤ俊鎭洖鐏岀粰 LLM锛孉gent 鍙崲绛栫暐閲嶈瘯锛屼富寰幆涓嶅穿婧冦€?|

---

## 闄勶細浠ｇ爜鏂囦欢绱㈠紩閫熸煡

| 鎶€鏈偣 | 鏍稿績鏂囦欢 | 鍏抽敭鏂规硶 |
|--------|---------|---------|
| 鍙屼笂涓嬫枃鍘嬬缉 | `ConversationHistoryCompactor.java` | `compact()`, `compactIfNeeded()`, `summarize()` |
| 鐭湡璁板繂鎽樿 | `ContextCompressor.java` | `compress()`, `extractFacts()`, `mapPhase()`, `reducePhase()` |
| 鑷姩闃堝€兼淳鐢?| `ContextProfile.java` | `from()`, `autoCompactTriggerTokens()`, `compressionTriggerRatio()` |
| Token 棰勭畻 | `TokenBudget.java` | `getAvailableForConversation()`, `estimateMessagesTokens()`, `isWithinBudget()` |
| Memory 闂ㄩ潰 | `MemoryManager.java` | `compressIfNeeded()`, `storeFact()`, `buildContextForQuery()` |
| 澶?Agent 缂栨帓 | `AgentOrchestrator.java` | `run()`, `parsePlan()`, `getExecutableSteps()`, `runBatchParallel()`, `runStep()` |
| Worker Pool | `AgentOrchestrator.java` | `BlockingQueue<SubAgent> workerPool`, `take()/offer()` |
| 鐙珛缂撳啿/鎸夊簭 flush | `AgentOrchestrator.java` | `ByteArrayOutputStream buffers`, 瀹屾垚鍚庢寜 `step_id` 閬嶅巻 flush |
| ReAct 寰幆 | `Agent.java` | `run()`, `maybeCompactHistory()`, `budget.check()` |
| Agent 棰勭畻 | `AgentBudget.java` | `check()`, `recordToolCalls()`, `signatureOf()` |
| JSON-RPC 瀹㈡埛绔?| `JsonRpcClient.java` | `request()`, `handleMessage()`, `sendNotification()` |
| Stdio 浼犺緭 | `StdioTransport.java` | `send()`, `startStdoutReader()`, `startStderrReader()`, `close()` |
| HTTP 浼犺緭 | `StreamableHttpTransport.java` | `send()`, `close()`, `parseSse()` |
| Server 绠＄悊鍣?| `McpServerManager.java` | `startAll()`, `start()`, `replaceTools()`, `registerNotificationHandlers()` |
| 鍛藉悕绌洪棿闅旂 | `McpToolDescriptor.java` | `namespaced()`, `serverName`, `namespacedName` |
| 鐑敞鍐?| `McpServerManager.java` | `registerNotificationHandlers()`, `replaceTools()`, `buildToolList()` |
| MCP 閰嶇疆鍔犺浇 | `McpConfigLoader.java` | `load()`, `prepare()`, `expandString()`, `addBuiltInStepSearchIfAvailable()` |
| Server 閰嶇疆妯″瀷 | `McpServerConfig.java` | `command`/`args`/`url`/`headers`/`disabled`, `isStdio()`/`isHttp()` |
| MCP 瀹℃壒璇嗗埆 | `ApprovalPolicy.java` | `isMcpTool()`, `mcpServerName()` |
| Prompt 缁勮 | `PromptAssembler.java` | `assemble()`, `applyVariables()`, `dynamicSection()` |
| 涓夊眰瑕嗙洊 | `PromptRepository.java` | `loadRequired()`, `loadBuiltin()`, `overrideIfPresent()` |
| Prompt 涓婁笅鏂?| `PromptContext.java` | Builder 妯″紡 |
| 妯″紡鏋氫妇 | `PromptMode.java` | `AGENT`, `PLAN`, `TEAM_PLANNER`, `TEAM_WORKER`, `TEAM_REVIEWER` |
| LLM 鎺ュ彛 | `LlmClient.java` | `maxContextWindow()`, `supportsPromptCaching()`, `promptCacheMode()` |
| 宸ュ叿娉ㄥ唽 | `ToolRegistry.java` | `registerMcpTool()`, `replaceMcpToolOutputsForServer()`, `executeTool()` |
