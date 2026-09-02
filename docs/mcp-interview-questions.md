# MCP 妯″潡鍏ㄦ櫙闈㈣瘯棰橈紙30 棰橈級

> 鍩轰簬 CodeCLI 椤圭洰鐨?MCP 瀹炵幇锛岃鐩栧崗璁璁°€佷紶杈撳眰銆丣SON-RPC銆佺敓鍛藉懆鏈熺鐞嗐€佸畨鍏ㄥ璁＄瓑鏍稿績鐭ヨ瘑鐐广€?
---

## 涓€銆佸崗璁笌鏋舵瀯璁捐

### Q1锛氫粈涔堟槸 MCP锛熶负浠€涔?CodeCLI 瑕佹帴鍏?MCP 鐢熸€侊紵

**绛?*锛歁CP锛圡odel Context Protocol锛夋槸 Anthropic 鎻愬嚭鐨勫紑鏀惧崗璁紝璁?AI 搴旂敤鑳戒互鏍囧噯鍖栨柟寮忔帴鍏ュ閮ㄥ伐鍏枫€佽祫婧愬拰鎻愮ず妯℃澘銆傜被姣旓細USB-C 鏄‖浠剁殑鏍囧噯鎺ュ彛锛孧CP 鏄?AI 宸ュ叿鐨勬爣鍑嗘帴鍙ｃ€?
CodeCLI 鎺ュ叆 MCP 鐨勪环鍊硷細
1. **鐢熸€佸鐢?*锛氱洿鎺ョ敤绀惧尯宸叉湁鐨?100+ MCP Server锛堟枃浠剁郴缁熴€丟it銆佹祻瑙堝櫒銆佹暟鎹簱绛夛級锛屼笉鐢ㄨ嚜宸卞啓
2. **瑙ｈ€?*锛氬伐鍏峰疄鐜板拰 Agent 閫昏緫鍒嗙锛孲erver 鍙互鐙珛鍗囩骇
3. **鏍囧噯鍖?*锛氫笌 Claude Code銆丆ursor 绛夊伐鍏烽厤缃吋瀹癸紝鐢ㄦ埛杩佺Щ鎴愭湰浣?
---

### Q2锛歅aiCLI 鐨?MCP 妯″潡鏁翠綋鏋舵瀯鏄€庢牱鐨勶紵鐢讳竴涓嬪垎灞傚浘銆?
**绛?*锛?
```
鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?                     CLI Layer                               鈹?鈹? /mcp, /mcp restart, /mcp logs, /mcp disable/enable         鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                              鈫?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?                  McpServerManager                           鈹?鈹? 澶?Server 鐢熷懡鍛ㄦ湡绠＄悊銆佸苟琛屽惎鍔ㄣ€佺姸鎬佽拷韪€佸伐鍏锋敞鍐?         鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                              鈫?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?                     McpClient                               鈹?鈹? 鍗?Server 闂ㄩ潰锛歩nitialize / listTools / callTool            鈹?鈹? listResources / readResource / listPrompts                  鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                              鈫?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?                   JsonRpcClient                              鈹?鈹? 璇锋眰/鍝嶅簲閰嶅銆侀€氱煡璺敱銆佽秴鏃躲€侀敊璇爜                          鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?                              鈫?鈹屸攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?鈹?   StdioTransport        鈹?   StreamableHttpTransport        鈹?鈹? ProcessBuilder + 涓夋祦   鈹? OkHttp + SSE + Session ID        鈹?鈹斺攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹粹攢鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?```

宸︿晶杩樻湁妯垏鍏虫敞鐐癸細
- `config/`锛氶厤缃姞杞斤紙涓ゅ眰閰嶇疆 + 鐜鍙橀噺锛?- `protocol/`锛歁CP 娑堟伅瀹氫箟 + Schema 娓呮礂
- `resources/`锛歊esource 缂撳瓨 + 铏氭嫙宸ュ叿
- `mention/`锛欯-mention 瑙ｆ瀽涓庡睍寮€
- `notifications/`锛氳鍔ㄩ€氱煡璺敱

---

### Q3锛氫负浠€涔堝悓鏃舵敮鎸?stdio 鍜?Streamable HTTP 涓ょ浼犺緭鍗忚锛?
**绛?*锛氫袱绉嶅崗璁鐩栦笉鍚屽満鏅細

| 缁村害 | stdio | Streamable HTTP |
|---|---|---|
| 閮ㄧ讲鏂瑰紡 | 鏈湴瀛愯繘绋?| 杩滅▼鏈嶅姟 |
| 寤惰繜 | 浣庯紙杩涚▼闂撮€氫俊锛?| 涓紙缃戠粶寰€杩旓級 |
| 鍏稿瀷鍦烘櫙 | filesystem銆乬it銆乧hrome-devtools | 浜戞湇鍔°€佸叡浜?API |
| 渚濊禆 | 闇€瑕佹湰鍦板畨瑁咃紙npx/uvx锛?| 鍙渶缃戠粶鍙揪 |
| 瀹夊叏 | 鏈湴鎵ц锛屾潈闄愬彈闄愪簬 OS | 闇€瑕侀壌鏉冿紙Bearer token锛?|

MCP 鍗忚瑙勮寖瑕佹眰 Client 鍚屾椂鏀寔涓ょ锛岀ぞ鍖?Server 涔熶袱绉嶉兘鏈夈€侾aiCLI 鐢ㄩ厤缃枃浠剁殑 `command` vs `url` 瀛楁鍖哄垎銆?
---

### Q4锛歁CP 鐨勫伐鍏峰懡鍚嶇┖闂翠负浠€涔堣璁℃垚 `mcp__{server}__{tool}`锛?
**绛?*锛氫笁涓師鍥狅細

1. **閬垮厤鍐茬獊**锛氫笉鍚?Server 鍙兘鏈夊悓鍚嶅伐鍏凤紙姣斿涓や釜 Server 閮藉彨 `read_file`锛夛紝鍔犲墠缂€鍖哄垎
2. **鏉ユ簮鍙拷婧?*锛歀LM 鍜岀敤鎴蜂竴鐪艰兘鐪嬪嚭宸ュ叿鏉ヨ嚜鍝釜 Server锛屼究浜庤皟璇?3. **涓?Claude Code 瀵归綈**锛欳laude Code 鐢ㄥ悓鏍锋牸寮忥紝閰嶇疆鏂囦欢鍙互澶嶇敤

```java
// McpToolDescriptor.java
public static String namespaced(String serverName, String toolName) {
    return "mcp__" + serverName + "__" + toolName;
}
// 缁撴灉锛歮cp__filesystem__read_file, mcp__chrome-devtools__navigate_page
```

---

## 浜屻€佷紶杈撳眰瀹炵幇

### Q5锛歴tdio Transport 鐨?涓夋祦绠＄悊"鏄粈涔堬紵涓轰粈涔?stderr 蹇呴』鍗曠嫭 drain锛?
**绛?*锛氬瓙杩涚▼鏈変笁涓爣鍑嗘祦锛?
```
stdin  鈫?鍐欏叆璇锋眰锛圔ufferedWriter + flush锛?stdout 鈫?璇诲彇鍝嶅簲锛堝崟鐙?daemon thread锛宯ewline-delimited JSON锛?stderr 鈫?璇诲彇閿欒/鏃ュ織锛堝崟鐙?daemon thread锛岀幆褰?buffer锛?```

**stderr 蹇呴』 drain 鐨勫師鍥?*锛氭搷浣滅郴缁熷绠￠亾鏈夌紦鍐插尯锛堥€氬父 64KB锛夈€傚鏋?stderr 娌′汉璇伙紝缂撳啿鍖烘弧浜嗗瓙杩涚▼鐨?`write(stderr)` 浼氶樆濉烇紝杩涜€屽鑷村瓙杩涚▼鍗℃锛屽弽杩囨潵鍙堝鑷?stdout 涔熸病杈撳嚭鈥斺€?*姝婚攣**銆?
```java
// StdioTransport.java 鍏抽敭浠ｇ爜
Thread stderrThread = new Thread(() -> {
    while ((line = stderrReader.readLine()) != null) {
        stderrBuffer.add(line);  // 鐜舰 buffer锛屼繚鐣欐渶杩?200 琛?    }
});
stderrThread.setDaemon(true);
stderrThread.start();
```

---

### Q6锛歴tdio Transport 鐨勪紭闆呭叧闂祦绋嬫槸鎬庢牱鐨勶紵

**绛?*锛?
```
1. 鍙戦€?shutdown 閫氱煡锛堝彲閫夛紝褰撳墠宸茬Щ闄ら伩鍏嶉樆濉烇級
2. 鍏抽棴 stdin锛堝彂閫?EOF 淇″彿锛?3. 绛夊緟 1 绉掞紙缁欏瓙杩涚▼娓呯悊鏃堕棿锛?4. process.destroy()锛堝彂閫?SIGTERM锛?5. 绛夊緟 2 绉?6. process.destroyForcibly()锛堝彂閫?SIGKILL锛?7. JVM shutdown hook 鍏滃簳锛堥槻姝㈠兊灏歌繘绋嬶級
```

涓轰粈涔堣澶氭锛熺洿鎺?`destroyForcibly()` 鍙兘瀵艰嚧瀛愯繘绋嬫潵涓嶅強娓呯悊涓存椂鏂囦欢鎴栭噴鏀鹃攣銆?
---

### Q7锛歋treamable HTTP 鐨?Session ID 鏄€庝箞绠＄悊鐨勶紵

**绛?*锛?
```
Client                          Server
  鈹?                              鈹?  鈹傗攢鈹€ POST /mcp 鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈫掆攤
  鈹傗啇鈹€ 200 OK 鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?   Mcp-Session-Id: abc123     鈹?  鈹?                              鈹?  鈹傗攢鈹€ POST /mcp 鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈫掆攤
  鈹?   Mcp-Session-Id: abc123     鈹?  鈹傗啇鈹€ SSE stream 鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹?  鈹?                              鈹?  鈹傗攢鈹€ DELETE /mcp 鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈹€鈫掆攤  // 鍏抽棴鏃堕噴鏀?  鈹?   Mcp-Session-Id: abc123     鈹?```

- 棣栨璇锋眰锛孲erver 杩斿洖 `Mcp-Session-Id` header
- 鍚庣画璇锋眰甯︿笂杩欎釜 header锛孲erver 璇嗗埆浼氳瘽
- 鍏抽棴鏃跺彂 DELETE 閲婃斁 Server 绔祫婧?- 濡傛灉 Server 涓嶈繑鍥?Session ID锛岄€€鍖栦负鏃犵姸鎬佽姹?
---

### Q8锛歋treamable HTTP 濡備綍澶勭悊 SSE 鍜屾櫘閫?JSON 涓ょ鍝嶅簲锛?
**绛?*锛?
```java
// StreamableHttpTransport.java
Response response = client.newCall(request).execute();
String contentType = response.header("Content-Type", "");

if (contentType.contains("text/event-stream")) {
    // SSE 娴佸紡鍝嶅簲锛氶€愯璇诲彇 data: 鍓嶇紑鐨?JSON
    BufferedReader reader = new BufferedReader(response.body().charStream());
    String line;
    while ((line = reader.readLine()) != null) {
        if (line.startsWith("data: ")) {
            handleMessage(parseJson(line.substring(6)));
        }
    }
} else {
    // 鏅€?JSON 鍝嶅簲锛氫竴娆℃€ц鍙?    handleMessage(parseJson(response.body().string()));
}
```

MCP 瑙勮寖鍏佽 Server 鑷富閫夋嫨鍝嶅簲鏍煎紡锛孋lient 蹇呴』鍏煎涓ょ銆?
---

## 涓夈€丣SON-RPC 瀹炵幇

### Q9锛欽SON-RPC 2.0 鍗忚鐨勬牳蹇冭绱犳湁鍝簺锛?
**绛?*锛?
```json
// 璇锋眰
{"jsonrpc": "2.0", "id": 1, "method": "tools/call", "params": {...}}

// 鎴愬姛鍝嶅簲
{"jsonrpc": "2.0", "id": 1, "result": {...}}

// 閿欒鍝嶅簲
{"jsonrpc": "2.0", "id": 1, "error": {"code": -32601, "message": "Method not found"}}

// 閫氱煡锛堟棤 id锛屼笉闇€瑕佸搷搴旓級
{"jsonrpc": "2.0", "method": "notifications/progress", "params": {...}}
```

鏍囧噯閿欒鐮侊細
- `-32700`锛歅arse error锛圝SON 瑙ｆ瀽澶辫触锛?- `-32600`锛欼nvalid Request锛堟牸寮忛敊璇級
- `-32601`锛歁ethod not found
- `-32602`锛欼nvalid params
- `-32603`锛欼nternal error

---

### Q10锛氫负浠€涔堣姹?ID 鍙敤 Long 鑰屼笉鐢?String锛?
**绛?*锛欽SON-RPC 瑙勮寖鍏佽 id 鏄?string 鎴?number锛屼絾 CodeCLI 閫夋嫨鍙敤 Long锛?
1. **绠€鍖栧疄鐜?*锛歚ConcurrentHashMap<Long, Future>` 姣?`ConcurrentHashMap<String, Future>` 鎬ц兘濂斤紙Long 鐨?hashCode 灏辨槸鑷韩鍊硷級
2. **MCP 鍗忚绾﹀畾**锛歁CP 瑙勮寖鎺ㄨ崘鐢ㄩ€掑鏁存暟
3. **澶熺敤**锛歀ong 鏈€澶у€?9.2脳10鹿鈦革紝涓嶅彲鑳界敤瀹?4. **璋冭瘯鍙嬪ソ**锛歩d=1,2,3...姣?UUID 濂借拷韪?
---

### Q11锛氶€氱煡锛圢otification锛夊拰璇锋眰锛圧equest锛夊湪浠ｇ爜涓浣曞尯鍒嗗鐞嗭紵

**绛?*锛?
```java
private void handleMessage(JsonNode message) {
    JsonNode idNode = message.get("id");
    
    if (idNode == null || idNode.isNull()) {
        // 閫氱煡锛氭病鏈?id锛屼笉闇€瑕佸搷搴?        for (Consumer<JsonNode> listener : notificationListeners) {
            listener.accept(message);
        }
        return;
    }
    
    // 鍝嶅簲锛氭湁 id锛屼粠 pending 鍙栧嚭瀵瑰簲 future
    long id = idNode.asLong();
    CompletableFuture<JsonNode> future = pending.remove(id);
    if (future == null) {
        return;  // 宸茶秴鏃舵垨閲嶅鍝嶅簲
    }
    
    JsonNode error = message.get("error");
    if (error != null && !error.isNull()) {
        future.completeExceptionally(new JsonRpcException(...));
    } else {
        future.complete(message.get("result"));
    }
}
```

閫氱煡鏄?鍙戜簡灏变笉绠?锛屼笉闇€瑕侀厤瀵癸紱璇锋眰蹇呴』绛夊緟鍝嶅簲銆?
---

### Q12锛氫负浠€涔堢敤 `ConcurrentHashMap<Long, CompletableFuture<JsonNode>>` 鍋氳姹傚搷搴旈厤瀵癸紵

**绛?*锛氳繖鏄?*寮傛璇锋眰-鍝嶅簲閰嶅**鐨勭粡鍏告ā寮忥紝鏍稿績瑙ｅ喅鐨勯棶棰樻槸锛?*澶氫釜骞跺彂璇锋眰鍚屾椂鍙戝嚭锛屽搷搴斿彲鑳戒贡搴忚繑鍥烇紝濡備綍鎶婂搷搴旀纭尮閰嶅埌瀵瑰簲鐨勮姹傦紵**

涓変釜鍏抽敭缁勪欢鐨勫垎宸ワ細

```java
// 1. AtomicLong - 鐢熸垚鍏ㄥ眬鍞竴銆侀€掑鐨勮姹?ID
private final AtomicLong ids = new AtomicLong(1);

// 2. ConcurrentHashMap - 瀛樺偍銆屽緟瀹屾垚銆嶇殑璇锋眰
//    key = 璇锋眰ID, value = 杩欎釜璇锋眰鐨勩€屾湭鏉ョ粨鏋溿€?private final ConcurrentHashMap<Long, CompletableFuture<JsonNode>> pending = new ConcurrentHashMap<>();

// 3. CompletableFuture - 姣忎釜璇锋眰鐨勫紓姝ョ粨鏋滃鍣?CompletableFuture<JsonNode> future = new CompletableFuture<>();
pending.put(id, future);  // 娉ㄥ唽锛氭垜鍦ㄧ瓑 id 杩欎釜鍝嶅簲
```

**涓轰粈涔堜笉鐢ㄥ叾浠栨柟妗?*锛?- 鍚屾闃诲锛氭晥鐜囧お浣庯紝MCP 宸ュ叿璋冪敤缁忓父骞惰
- 杞鍖归厤锛欳PU 绌鸿浆銆佸欢杩熼珮
- 鍥炶皟鍑芥暟锛氬洖璋冨湴鐙憋紝寮傚父澶勭悊鍥伴毦

---

## 鍥涖€丼erver 鐢熷懡鍛ㄦ湡绠＄悊

### Q13锛歁cpServerManager 濡備綍瀹炵幇骞惰鍚姩锛熷崟涓?Server 澶辫触浼氬奖鍝嶅叾浠?Server 鍚楋紵

**绛?*锛?
```java
// McpServerManager.java
ExecutorService executor = Executors.newFixedThreadPool(
    Math.min(targets.size(), 8),  // 鏈€澶?8 涓苟琛?    r -> new Thread(r, "CodeCLI-mcp-startup-" + threadId.incrementAndGet())
);

List<CompletableFuture<Void>> futures = targets.stream()
    .map(server -> CompletableFuture.runAsync(() -> start(server), executor))
    .toList();

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
```

**鍗曚釜澶辫触涓嶅奖鍝嶅叾浠?*锛?```java
private void start(McpServer server) {
    try {
        // 鍒濆鍖栨祦绋?..
        server.status(McpServerStatus.READY);
    } catch (Exception e) {
        server.errorMessage(e.getMessage());
        server.status(McpServerStatus.ERROR);  // 鍙爣璁拌嚜宸?    }
}
```

姣忎釜 Server 鐨勫惎鍔ㄥ湪鐙珛鐨?try-catch 涓紝澶辫触鍙奖鍝嶈嚜宸便€?
---

### Q14锛氬惎鍔ㄦ椂鐨勮繘搴︽墦鍗版槸鎬庝箞瀹炵幇鐨勶紵涓轰粈涔堣鍗曠嫭璧风嚎绋嬶紵

**绛?*锛?
```java
Thread progressPrinter = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        TimeUnit.SECONDS.sleep(5);  // 姣?5 绉掓墦鍗颁竴娆?        for (McpServer server : targets) {
            if (server.status() == McpServerStatus.STARTING) {
                long waited = Duration.between(startedAt, Instant.now()).toSeconds();
                out.printf("   鈴?%-16s 鍚姩涓?..锛堝凡绛夊緟 %ds锛?n", server.name(), waited);
            }
        }
    }
});
```

**鍗曠嫭璧风嚎绋嬬殑鍘熷洜**锛歚startAll()` 涓荤嚎绋嬭 `CompletableFuture.allOf().join()` 闃诲锛屾棤娉曞悓鏃舵墦鍗拌繘搴︺€傝繘搴︾嚎绋嬫槸 daemon 绾跨▼锛屼富娴佺▼缁撴潫鍚庤嚜鍔ㄩ€€鍑恒€?
---

### Q15锛歋erver 宕╂簝锛坰tdout 娴佹柇寮€锛夊悗浼氬彂鐢熶粈涔堬紵

**绛?*锛氬綋鍓嶈璁℃槸**涓嶈嚜鍔ㄩ噸鍚?*锛?
1. stdout 璇诲彇绾跨▼妫€娴嬪埌娴佸叧闂?2. `McpServer.status` 鏍囪涓?`ERROR`
3. 璇?Server 鐨勬墍鏈夊伐鍏蜂粠 `ToolRegistry` 绉婚櫎
4. 鐢ㄦ埛閫氳繃 `/mcp logs <name>` 鏌ョ湅 stderr 鏃ュ織鎺掓煡鍘熷洜
5. 鐢ㄦ埛鎵嬪姩 `/mcp restart <name>` 鎭㈠

**涓轰粈涔堜笉鍋氳嚜鍔ㄩ噸鍚?*锛?- 閬垮厤鏃犻檺閲嶅惎寰幆锛堥厤缃敊璇鑷村弽澶嶅穿婧冿級
- 璁╃敤鎴风煡閬撳嚭浜嗛棶棰?- 绗竴鐗堜繚鎸佺畝鍗曪紝鍚庣画鍐嶅姞閫€閬块噸鍚?
---

## 浜斻€佸伐鍏锋敞鍐屼笌 Schema 澶勭悊

### Q16锛歁CP 宸ュ叿鏄浣曟敞鍐屽埌 ToolRegistry 鐨勶紵

**绛?*锛?
```java
// McpServerManager.java
private void replaceTools(McpServer server, McpClient client, List<McpToolDescriptor> tools) {
    toolRegistry.replaceMcpToolOutputsForServer(
        server.name(), 
        tools,
        descriptor -> args -> invokeMcpToolOutput(client, descriptor, args)
    );
}

// ToolRegistry.java
public void replaceMcpToolOutputsForServer(String serverName, 
                                            List<McpToolDescriptor> tools,
                                            Function<McpToolDescriptor, Function<String, ToolOutput>> invokerFactory) {
    // 1. 绉婚櫎璇?Server 鏃х殑宸ュ叿
    mcpTools.keySet().removeIf(name -> name.startsWith("mcp__" + serverName + "__"));
    
    // 2. 娉ㄥ唽鏂扮殑宸ュ叿
    for (McpToolDescriptor tool : tools) {
        mcpTools.put(tool.namespacedName(), invokerFactory.apply(tool));
    }
}
```

宸ュ叿娉ㄥ唽鏄?*鍘熷瓙鏇挎崲**锛氬厛绉婚櫎鏃х殑锛屽啀娉ㄥ唽鏂扮殑锛岄伩鍏嶄腑闂寸姸鎬併€?
---

### Q17锛氫负浠€涔堣瀵?MCP Server 杩斿洖鐨?JSON Schema 鍋氭竻娲楋紵

**绛?*锛氬洜涓?LLM锛圙LM-5.1銆丏eepSeek 绛夛級瀵?JSON Schema 鐨勬敮鎸佷笉瀹屾暣锛?
```java
// McpSchemaSanitizer.java
public static JsonNode sanitize(JsonNode schema) {
    // 1. 鍒犻櫎 $schema, $id, $ref锛圠LM 涓嶈璇嗭級
    // 2. 宓屽 anyOf/oneOf 闄嶇骇涓?type: object锛圠LM 涓嶆敮鎸佸鏉傝仈鍚堢被鍨嬶級
    // 3. 鎻忚堪瓒呰繃 1000 瀛楃鎴柇锛堥伩鍏?token 娴垂锛?}
```

**涓嶆竻娲楃殑鍚庢灉**锛?- `$ref` 瀵艰嚧 LLM 鎶ラ敊"鏃犳硶瑙ｆ瀽寮曠敤"
- `anyOf` 瀵艰嚧 LLM 鐢熸垚涓嶇鍚堜换浣曞垎鏀殑鍙傛暟
- 瓒呴暱鎻忚堪娴垂 token 棰勭畻

---

### Q18锛歵ools/call 杩斿洖鐨?`isError: true` 鎬庝箞澶勭悊锛?
**绛?*锛?
```java
// McpClient.java
public ToolOutput callToolOutput(String toolName, String argumentsJson) {
    McpCallToolResult callResult = MAPPER.treeToValue(result, McpCallToolResult.class);
    ToolOutput output = callResult.toToolOutput();
    
    if (callResult.isError()) {
        // 鎶婇敊璇寘瑁呮垚 LLM 鍙鐨勬秷鎭?        return new ToolOutput("MCP 宸ュ叿杩斿洖閿欒: " + output.text(), output.imageParts());
    }
    return output;
}
```

`isError: true` 涓嶆姏寮傚父锛岃€屾槸鎶?content 褰撴垚閿欒娑堟伅杩斿洖缁?LLM銆侺LM 鐪嬪埌閿欒鍚庡彲浠ワ細
1. 璋冩暣鍙傛暟閲嶈瘯
2. 鎹釜宸ュ叿
3. 鍛婄煡鐢ㄦ埛

---

## 鍏€侀厤缃鐞?
### Q19锛氫袱灞傞厤缃紙鐢ㄦ埛绾?+ 椤圭洰绾э級鏄浣曞悎骞剁殑锛?
**绛?*锛?
```
鐢ㄦ埛绾э細~/.CodeCLI/mcp.json      锛堝叏灞€榛樿锛?椤圭洰绾э細.CodeCLI/mcp.json        锛堥」鐩鐩栵級
```

**鍚堝苟瑙勫垯**锛氭寜 server 鍚?merge锛岄」鐩骇瑕嗙洊鐢ㄦ埛绾х殑鍚屽悕 server

```java
// McpConfigLoader.java
Map<String, McpServerConfig> userConfigs = loadFile(userConfigPath);
Map<String, McpServerConfig> projectConfigs = loadFile(projectConfigPath);

// 椤圭洰绾ц鐩栫敤鎴风骇
Map<String, McpServerConfig> merged = new LinkedHashMap<>(userConfigs);
merged.putAll(projectConfigs);  // 鍚屽悕 key 鐩存帴瑕嗙洊
```

**涓轰粈涔堣繖鏍疯璁?*锛?- 鐢ㄦ埛绾ф斁閫氱敤閰嶇疆锛坒ilesystem銆乬it锛?- 椤圭洰绾ф斁椤圭洰涓撳睘閰嶇疆锛堢壒瀹?API endpoint锛?- 椤圭洰绾у彲鎻愪氦鍒?git锛屽洟闃熷叡浜?
---

### Q20锛歚${VAR}` 鐜鍙橀噺鏇挎崲鏄€庝箞瀹炵幇鐨勶紵缂哄け鍙橀噺鎬庝箞澶勭悊锛?
**绛?*锛?
```java
// McpConfigLoader.java
private String expandVariables(String value) {
    // 鍖归厤 ${VAR} 妯″紡
    Matcher matcher = Pattern.compile("\\$\\{([^}]+)}").matcher(value);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
        String varName = matcher.group(1);
        String varValue = resolveVariable(varName);
        if (varValue == null) {
            throw new IOException("缂哄け鐜鍙橀噺: " + varName);  // 鐩存帴鎶ラ敊
        }
        matcher.appendReplacement(sb, Matcher.quoteReplacement(varValue));
    }
    return sb.appendTail().toString();
}

private String resolveVariable(String name) {
    // 浼樺厛绾э細绯荤粺灞炴€?> 鐜鍙橀噺 > .env 鏂囦欢 > 鍐呯疆鍙橀噺
    if ("PROJECT_DIR".equals(name)) return projectDir.toString();
    if ("HOME".equals(name)) return System.getProperty("user.home");
    return System.getProperty(name, System.getenv(name));
}
```

**缂哄け鍙橀噺鐩存帴鎶ラ敊**锛屼笉闈欓粯淇濈暀 `${VAR}`銆傝繖鏄湁鎰忎负涔嬶細璁╃敤鎴锋槑纭煡閬撳摢閲屾病閰嶅ソ锛岃€屼笉鏄繍琛屾椂鎵嶅彂鐜板伐鍏疯皟鐢ㄥけ璐ャ€?
---

## 涓冦€佸畨鍏ㄤ笌瀹¤

### Q21锛氭墍鏈?MCP 宸ュ叿閮借蛋 HITL 瀹℃壒鍚楋紵涓轰粈涔堬紵

**绛?*锛氭槸鐨勶紝鎵€鏈?`mcp__` 鍓嶇紑宸ュ叿榛樿璧?HITL锛?
```java
// ApprovalPolicy.java
public boolean requiresApproval(String toolName) {
    return DANGEROUS_TOOLS.contains(toolName) || toolName.startsWith("mcp__");
}
```

**鍘熷洜**锛?1. **涓嶅彲淇℃潵婧?*锛歁CP Server 鏄涓夋柟浠ｇ爜锛屽彲鑳芥湁鎭舵剰鎴?bug
2. **鑳藉姏鏈煡**锛氬伐鍏峰彲鑳借鍐欐枃浠躲€佹墽琛屽懡浠ゃ€佽闂綉缁?3. **瀹夊叏绗竴**锛氬畞鍙纭涓€娆★紝涔熶笉瑕佽鐢ㄦ埛鍦ㄤ笉鐭ユ儏鐨勬儏鍐典笅鎵ц鍗遍櫓鎿嶄綔

**浼樺寲**锛氱 13 鏈熷鍔犱簡 server 缁村害鍏ㄦ斁琛岋紝杩炵画娴忚鍣ㄦ搷浣滃彧闇€纭涓€娆°€?
---

### Q22锛氬璁℃棩蹇楃殑鍙傛暟鑴辨晱鏄€庝箞鍋氱殑锛?
**绛?*锛?
```java
// AuditLog.java
public static String sanitize(String args) {
    if (args == null) return "";
    
    // 1. Bearer token
    args = args.replaceAll("Bearer [\\w\\-\\.]+", "Bearer ***");
    
    // 2. key/value 褰㈠紡鐨勬晱鎰熷瓧娈?    args = args.replaceAll("(?i)(token|key|password|secret|authorization)[\"\\s:=]+[\"']?[\\w\\-]+", 
                           "$1=***");
    
    return args;
}
```

**鑴辨晱绀轰緥**锛?```
鍘熷锛歿"Authorization": "Bearer eyJhbGciOiJIUzI1NiJ9.xxx"}
鑴辨晱锛歿"Authorization": "Bearer ***"}

鍘熷锛歿"password": "mySecret123"}
鑴辨晱锛歿"password": "***"}
```

**涓轰粈涔堝繀椤昏劚鏁?*锛氬璁℃棩蹇楀啓鍒?`~/.CodeCLI/audit/`锛屽彲鑳借鍏朵粬宸ュ叿璇诲彇鎴栫敤鎴峰垎浜紝涓嶈兘娉勯湶鍑瘉銆?
---

## 鍏€丷esources 涓?Prompts

### Q23锛歁CP Resources 鐨?鍙岃建鏀寔"鏄粈涔堬紵

**绛?*锛?
**杞ㄩ亾1锛氬伐鍏峰眰**锛圠LM 鑷姩璋冪敤锛?```
mcp__filesystem__list_resources   鈫?鍒楀嚭鎵€鏈夎祫婧?mcp__filesystem__read_resource    鈫?璇诲彇鎸囧畾璧勬簮
```
LLM 鍦ㄥ璇濅腑鑷姩鍒ゆ柇鏄惁闇€瑕佽鍙栬祫婧愩€?
**杞ㄩ亾2锛氱敤鎴疯緭鍏ュ眰**锛堢敤鎴锋樉寮忓紩鐢級
```
甯垜鐪嬩笅 @filesystem:file://README.md 杩欎唤鏂囨。
```
鐢ㄦ埛鐢?`@server:uri` 璇硶鏄惧紡寮曠敤锛屾彁浜ょ粰 Agent 鍓嶅睍寮€涓?`<resource>` 鍐呰仈鍧椼€?
**涓轰粈涔堣鍙岃建**锛?- 宸ュ叿灞傦細LLM 鑷富鍐崇瓥锛岄€傚悎"甯垜鎵炬壘椤圭洰閲屾湁娌℃湁閰嶇疆鏂囦欢"
- 鐢ㄦ埛杈撳叆灞傦細鐢ㄦ埛鏄庣‘鎸囧畾锛岄€傚悎"甯垜鐪嬭繖涓枃浠?

---

### Q24锛欯-mention 鐨勮В鏋愭祦绋嬫槸鎬庢牱鐨勶紵

**绛?*锛?
```java
// AtMentionParser.java
// 璇硶锛欯([a-zA-Z][\w-]*):([a-z]+)://([^\s@]+)
// 绀轰緥锛欯filesystem:file://README.md

// AtMentionExpander.java
public String expand(String input) {
    Matcher matcher = MENTION_PATTERN.matcher(input);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
        String server = matcher.group(1);  // "filesystem"
        String uri = matcher.group(2) + "://" + matcher.group(3);  // "file://README.md"
        
        // 璇诲彇璧勬簮鍐呭
        ResourceReadResult result = serverManager.readResourceForMention(server, uri);
        
        // 鏇挎崲涓?<resource> 鍐呰仈鍧?        matcher.appendReplacement(sb, "<resource server=\"" + server + "\" uri=\"" + uri + "\">" 
                                  + result.content() + "</resource>");
    }
    return sb.appendTail().toString();
}
```

**灞曞紑鍓嶅悗瀵规瘮**锛?```
杈撳叆锛氬府鎴戠湅涓?@filesystem:file://README.md 杩欎唤鏂囨。
杈撳嚭锛氬府鎴戠湅涓?<resource server="filesystem" uri="file://README.md">
      # CodeCLI
      ...锛堟枃浠跺唴瀹癸級
      </resource> 杩欎唤鏂囨。
```

---

### Q25锛氳鍔ㄩ€氱煡锛坣otifications/tools/list_changed锛夋槸鎬庝箞澶勭悊鐨勶紵

**绛?*锛?
```java
// NotificationRouter.java
router.on("notifications/tools/list_changed", ignored -> {
    // 宸ュ叿鍒楄〃鍙樹簡锛岄噸鏂版媺鍙栧苟鍘熷瓙鏇挎崲
    List<McpToolDescriptor> tools = buildToolList(server, client);
    replaceTools(server, client, tools);
    server.tools(tools);
});

router.on("notifications/resources/list_changed", ignored -> {
    // 璧勬簮鍒楄〃鍙樹簡锛屾竻绌虹紦瀛?    resourceCache.invalidateServer(server.name());
});

router.on("notifications/resources/updated", params -> {
    // 鍗曚釜璧勬簮鏇存柊锛屽彧澶辨晥杩欎釜璧勬簮
    String uri = params.path("uri").asText("");
    resourceCache.invalidateResource(server.name(), uri);
});
```

**涓轰粈涔堟槸"琚姩"**锛欳lient 涓嶄富鍔ㄨ疆璇紝鑰屾槸 Server 鏈夊彉鍖栨椂涓诲姩閫氱煡銆傝繖鏄?MCP 鍗忚鐨勬帹閫佹ā鍨嬨€?
---

## 涔濄€丆hrome DevTools 闆嗘垚

### Q26锛氫负浠€涔?Chrome DevTools MCP 榛樿 enabled锛?
**绛?*锛?
1. **鏍稿績鍦烘櫙**锛氱敤鎴蜂富瑕佽瘔姹傚氨鏄?璁?Agent 鑳戒笂娴忚鍣?锛屽叧闂瓑浜庢妸鏍稿績鍔熻兘钘忚捣鏉?2. **棣栨浣撻獙**锛氬鏋滈粯璁ゅ叧闂紝鐢ㄦ埛闇€瑕侀澶栭厤缃墠鑳界敤锛屽鍔犳懇鎿?3. **鍙帶浠ｄ环**锛?8 涓伐鍏峰鍔?token 鍗犵敤锛屼絾鐢ㄦ埛鍙互 `/mcp disable` 涓存椂鍏抽棴

**鏉冭　**锛氬伐鍏峰垪琛ㄨ啫鑳€锛?0+ 宸ュ叿锛夌殑浠ｄ环 < 鐢ㄦ埛棣栨浣跨敤鐨勪綋楠岄樆鍔涖€?
---

### Q27锛欻ITL 鐨?server 缁村害鍏ㄦ斁琛?鏄€庝箞瀹炵幇鐨勶紵

**绛?*锛?
```java
// TerminalHitlHandler.java
private Set<String> approvedAllByTool = new HashSet<>();    // 宸ュ叿缁村害
private Set<String> approvedAllByServer = new HashSet<>();  // Server 缁村害

// 鍒ゆ柇鏄惁璺宠繃瀹℃壒
public boolean shouldSkipApproval(String toolName) {
    // 1. 妫€鏌ュ伐鍏风淮搴?    if (approvedAllByTool.contains(toolName)) return true;
    
    // 2. 妫€鏌?Server 缁村害
    if (toolName.startsWith("mcp__")) {
        String serverName = extractServerName(toolName);  // "chrome-devtools"
        if (approvedAllByServer.contains(serverName)) return true;
    }
    
    return false;
}

// 鐢ㄦ埛閫?"a 鈫?server" 鏃?case APPROVED_ALL_BY_SERVER:
    approvedAllByServer.add(extractServerName(toolName));
    break;
```

**鏁堟灉**锛氱敤鎴峰 `mcp__chrome-devtools__navigate_page` 閫?鍏ㄩ儴鏀捐 server"鍚庯紝鍚庣画 `mcp__chrome-devtools__click`銆乣mcp__chrome-devtools__take_snapshot` 绛夐兘涓嶅啀寮圭獥銆?
---

## 鍗併€佽璁℃潈琛′笌婕旇繘

### Q28锛氳繖涓?MCP 瀹炵幇鍜?Claude Code 鐨勬湁浠€涔堝紓鍚岋紵

**绛?*锛?
| 缁村害 | CodeCLI | Claude Code |
|---|---|---|
| 璇█ | Java | TypeScript |
| 閰嶇疆鏍煎紡 | 鍏煎锛坢cp.json锛?| 鍘熺敓 |
| 宸ュ叿鍛藉悕 | `mcp__{server}__{tool}` | 鐩稿悓 |
| 浼犺緭鍗忚 | stdio + Streamable HTTP | stdio + SSE |
| HITL | 榛樿寮€鍚?| 榛樿鍏抽棴 |
| 瀹¤鏃ュ織 | 鏈?| 鏃?|
| Resources | 鍙岃建鏀寔 | 浠呭伐鍏峰眰 |
| @-mention | 鏀寔 | 涓嶆敮鎸?|

**璁捐鍐崇瓥**锛歅aiCLI 鏇存敞閲嶅畨鍏紙HITL + 瀹¤锛夊拰鐢ㄦ埛浣撻獙锛園-mention锛夛紝Claude Code 鏇存敞閲嶇畝娲併€?
---

### Q29锛氬鏋?MCP Server 杩斿洖鐨勫伐鍏峰悕鍜屽唴缃伐鍏峰啿绐佹€庝箞鍔烇紵

**绛?*锛?
```java
// McpServerManager.java
private void validateNoDuplicateTools(String serverName, List<McpToolDescriptor> tools) {
    Map<String, Integer> counts = new LinkedHashMap<>();
    for (McpToolDescriptor tool : tools) {
        counts.merge(tool.name(), 1, Integer::sum);
    }
    List<String> duplicates = counts.entrySet().stream()
        .filter(e -> e.getValue() > 1)
        .map(Map.Entry::getKey)
        .toList();
    if (!duplicates.isEmpty()) {
        throw new IllegalArgumentException("MCP server " + serverName + " 杩斿洖閲嶅宸ュ叿鍚? " + duplicates);
    }
}
```

**澶勭悊绛栫暐**锛?1. **鍚?Server 鍐呴噸澶?*锛氱洿鎺ユ姤閿欙紝瑕佹眰鐢ㄦ埛鍦ㄩ厤缃噷 alias
2. **璺?Server 閲嶅**锛氬懡鍚嶇┖闂?`mcp__{server}__` 澶╃劧闅旂锛屼笉浼氬啿绐?3. **涓庡唴缃伐鍏峰啿绐?*锛氬唴缃伐鍏锋病鏈?`mcp__` 鍓嶇紑锛屼笉浼氬啿绐?
---

### Q30锛氳繖涓璁℃湁浠€涔堜笉瓒筹紵浣犱細鎬庝箞鏀硅繘锛?
**绛?*锛?
**涓嶈冻1锛氫笉鏀寔 OAuth**
- 褰撳墠鍙敮鎸侀潤鎬?Bearer token
- 鏀硅繘锛氬疄鐜?Authorization Code + PKCE 娴佺▼

**涓嶈冻2锛氫笉鏀寔 sampling锛圫erver 鍙嶅悜璋冪敤 LLM锛?*
- MCP 瑙勮寖鍏佽 Server 璇锋眰 LLM 鐢熸垚鍐呭
- 鏀硅繘锛氬疄鐜?`sampling/createMessage`锛屼絾瑕佸己鍒?HITL

**涓嶈冻3锛氫笉鏀寔鑷姩閲嶅惎**
- Server 宕╂簝鍚庨渶瑕佹墜鍔?`/mcp restart`
- 鏀硅繘锛氭寚鏁伴€€閬块噸鍚紝鏈€澶?3 娆?
**涓嶈冻4锛歋chema 娓呮礂鍙兘涓㈠け淇℃伅**
- `anyOf` 闄嶇骇涓?`type: object` 鍙兘瀵艰嚧 LLM 鐢熸垚閿欒鍙傛暟
- 鏀硅繘锛氫繚鐣欏師濮?schema 浣滀负 description 鐨勪竴閮ㄥ垎

---

## 闄勫綍锛氶潰璇曞洖绛旂瓥鐣?
1. **鍒嗗眰鍥炵瓟**锛氬厛璇存灦鏋勶紝鍐嶆繁鍏ョ粏鑺傦紝璁╅潰璇曞畼閫夋嫨杩介棶娣卞害
2. **缁撳悎婧愮爜**锛氭彁鍒板叿浣撶被鍚嶅拰鏂规硶锛岃瘉鏄庝綘璇昏繃浠ｇ爜
3. **涓诲姩瀵规瘮**锛氬拰 Claude Code銆佸叾浠栨鏋跺姣旓紝灞曠ず瑙嗛噹
4. **鎵胯杈圭晫**锛氳"褰撳墠娌″仛 X锛屽洜涓?Y锛屽鏋滈渶瑕佸彲浠?Z"
5. **璁捐鏉冭　**锛氭瘡涓喅绛栭兘鏈?trade-off锛屽睍绀轰綘鐨勬€濊€冭繃绋?
---

## 闄勫綍锛氬叧閿簮鐮佹枃浠剁储寮?
| 鏂囦欢 | 鑱岃矗 |
|---|---|
| `McpClient.java` | 鍗?Server 闂ㄩ潰锛屽崗璁眰璋冪敤 |
| `McpServerManager.java` | 澶?Server 鐢熷懡鍛ㄦ湡绠＄悊 |
| `McpServer.java` | 鍗?Server 杩愯鎬?|
| `JsonRpcClient.java` | JSON-RPC 璇锋眰/鍝嶅簲閰嶅 |
| `StdioTransport.java` | stdio 瀛愯繘绋嬩紶杈?|
| `StreamableHttpTransport.java` | HTTP + SSE 浼犺緭 |
| `McpConfigLoader.java` | 閰嶇疆鍔犺浇涓庡悎骞?|
| `McpSchemaSanitizer.java` | JSON Schema 娓呮礂 |
| `McpToolDescriptor.java` | 宸ュ叿鎻忚堪涓庡懡鍚嶇┖闂?|
| `NotificationRouter.java` | 琚姩閫氱煡璺敱 |
| `McpResourceCache.java` | Resource 缂撳瓨 |
| `AtMentionExpander.java` | @-mention 灞曞紑 |

