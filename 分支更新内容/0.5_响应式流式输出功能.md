🎉 **YuCodeMother v0.5 版本发布说明**

### 📋 版本概述

本次 v0.5 版本是项目的重要增强版本，在 v0.4 的基础上实现了 **SSE（Server-Sent Events）流式输出功能**。通过集成 Project Reactor 和 LangChain4j Reactor 模块，用户可以实时看到 AI 生成代码的过程，大幅提升了用户体验和交互性。

---

### ✨ 新增功能

#### 🌊 SSE 流式输出核心功能

**1. 响应式流式代码生成**
- ✅ **实时流式输出**：AI 生成的代码逐字符推送到前端
- ✅ **双模式流式支持**：HTML 单文件和多文件模式均支持流式输出
- ✅ **自动代码收集**：后台自动收集完整代码并保存
- ✅ **非阻塞异步处理**：基于 Reactor 的响应式编程模型
- ✅ **多订阅者支持**：使用 `.cache()` 允许多个订阅者

**2. 流式接口设计**
```java
public interface AiCodeGeneratorService {
    
    /**
     * 流式生成 HTML 代码
     * @param userMessage 用户提示词
     * @return 流式字符串输出
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    Flux<String> generateHtmlCodeStream(String userMessage);
    
    /**
     * 流式生成多文件代码
     * @param userMessage 用户提示词
     * @return 流式字符串输出
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    Flux<String> generateMultiFileCodeStream(String userMessage);
}
```

**3. 智能代码解析器**
- ✅ **正则表达式解析**：自动识别 Markdown 代码块
- ✅ **多语言支持**：支持 HTML、CSS、JavaScript 代码提取
- ✅ **容错处理**：代码块缺失时使用完整内容
- ✅ **格式规范化**：自动去除多余空白和格式化代码

#### 🏗️ 技术架构升级

**响应式编程模型**

**1. Flux 流式处理**
```java
private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
    Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
    StringBuilder codeBuilder = new StringBuilder();
    
    // 使用 cache() 缓存流数据，允许多次订阅
    return result.cache()
            .doOnNext(chunk -> {
                // 实时收集代码片段
                codeBuilder.append(chunk);
            })
            .doOnComplete(() -> {
                // 流式返回完成后，保存代码
                String completeHtmlCode = codeBuilder.toString();
                HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                File saveDir = CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
                log.info("文件保存成功，目录为：{}", saveDir.getAbsolutePath());
            });
}
```

**设计亮点**：
- **`.cache()`**：将冷流转换为热流，支持多个订阅者
- **`.doOnNext()`**：实时处理每个数据块，收集代码片段
- **`.doOnComplete()`**：流结束时触发文件保存逻辑
- **非阻塞**：整个过程异步执行，不阻塞主线程

**2. 代码解析器（CodeParser）**
```java
public class CodeParser {
    
    // 正则表达式模式
    private static final Pattern HTML_CODE_PATTERN = 
        Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = 
        Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = 
        Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    
    /**
     * 解析 HTML 单文件代码
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 容错：如果没找到代码块，将整个内容作为 HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }
    
    /**
     * 解析多文件代码（HTML + CSS + JS）
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        result.setHtmlCode(extractCodeByPattern(codeContent, HTML_CODE_PATTERN));
        result.setCssCode(extractCodeByPattern(codeContent, CSS_CODE_PATTERN));
        result.setJsCode(extractCodeByPattern(codeContent, JS_CODE_PATTERN));
        return result;
    }
}
```

**解析器特点**：
- **智能识别**：自动识别 Markdown 格式的代码块
- **多语言支持**：支持 `html`、`css`、`js`/`javascript` 标记
- **大小写不敏感**：使用 `Pattern.CASE_INSENSITIVE` 标志
- **容错机制**：代码块缺失时使用完整内容

**3. 门面模式扩展**
```java
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    
    /**
     * 统一入口：流式生成并保存代码
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }
        
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, 
                "不支持的生成类型" + codeGenTypeEnum.getValue());
        };
    }
}
```

**架构优势**：
- **统一接口**：流式和非流式使用相同的门面模式
- **类型路由**：通过枚举类型自动路由到对应方法
- **异常处理**：统一的参数校验和错误处理

#### 📦 依赖管理

**新增核心依赖**

**1. Project Reactor**
```xml
<!-- 12.引入 Project Reactor 用于流式响应 -->
<dependency>
    <groupId>io.projectreactor</groupId>
    <artifactId>reactor-core</artifactId>
</dependency>
```

**作用**：
- 提供 `Flux` 和 `Mono` 响应式类型
- 支持非阻塞异步流处理
- 实现背压（Backpressure）机制

**2. LangChain4j Reactor 集成**
```xml
<!-- 13.引入 LangChain4j Reactor 集成（支持 Flux 返回类型） -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-reactor</artifactId>
    <version>1.1.0-beta7</version>
</dependency>
```

**作用**：
- 将 LangChain4j 与 Reactor 集成
- 支持 `Flux<String>` 作为 AI 服务返回类型
- 自动处理流式 API 响应

#### 🎯 配置优化

**DeepSeek 流式模型配置**
```yaml
langchain4j:
  open-ai:
    # 非流式模型
    chat-model:
      base-url: https://api.deepseek.com
      api-key: sk-xxxxx
      model-name: deepseek-chat
      max-tokens: 8192
      strict-json-schema: true
      response-format: json_object
      log-requests: true
      log-responses: true
    
    # 流式模型（新增）
    streaming-chat-model:
      base-url: https://api.deepseek.com
      api-key: sk-xxxxx  # 使用相同的 API Key
      model-name: deepseek-chat
      max-tokens: 8192
      log-requests: true
      log-responses: true
```

**配置说明**：
- **两个独立配置**：`chat-model`（非流式）和 `streaming-chat-model`（流式）
- **相同 API Key**：可以使用同一个 DeepSeek API Key
- **不同参数**：流式模型不需要 `strict-json-schema` 和 `response-format`
- **日志记录**：两者都启用请求/响应日志便于调试

---

### 🔧 技术亮点

#### 1. Reactor 响应式流

**冷流 vs 热流**

**冷流（Cold Stream）**：
- 每次订阅都重新执行数据源
- 类似于点播视频，每个观众从头开始看
- LangChain4j 默认返回冷流

**热流（Hot Stream）**：
- 数据源只执行一次，多个订阅者共享数据
- 类似于直播，所有观众看同一个流
- 使用 `.cache()` 转换为热流

**问题场景**：
```java
// ❌ 错误：冷流被多次订阅
Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
return result.doOnNext(...).doOnComplete(...);
// 测试代码调用 collectList().block() 时会导致多次订阅错误
```

**解决方案**：
```java
// ✅ 正确：使用 cache() 转换为热流
return result.cache()
        .doOnNext(...)
        .doOnComplete(...);
```

#### 2. 正则表达式优化

**常见错误**：
```java
// ❌ 错误的正则表达式
Pattern.compile("```html\\s*\\s\\n([\\s\\s]*?)```")
//                           ^^^^  ^^^^^^^^
//                           多余   错误的字符类
```

**正确写法**：
```java
// ✅ 正确的正则表达式
Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE)
//                           ^^  ^^^^^^^^
//                           简洁  正确的字符类
```

**关键点**：
- `\\s*\\n`：匹配可选空白 + 换行符
- `[\\s\\S]`：匹配任意字符（空白 + 非空白）
- `*?`：非贪婪匹配，尽可能少匹配
- `Pattern.CASE_INSENSITIVE`：大小写不敏感

#### 3. 异步文件保存

**流式处理流程**：
```
AI 生成流 (Flux<String>)
    ↓
.cache() 转换为热流
    ↓
    ├─→ doOnNext() 实时收集代码片段
    │   └─→ StringBuilder.append(chunk)
    │
    ├─→ doOnComplete() 流结束时保存文件
    │   ├─→ CodeParser.parseHtmlCode()
    │   ├─→ CodeFileSaver.saveHtmlCodeResult()
    │   └─→ log.info("文件保存成功")
    │
    └─→ 返回给前端（SSE 推送）
```

**优势**：
- **实时反馈**：前端可以实时显示生成进度
- **自动保存**：后台自动收集并保存完整代码
- **非阻塞**：不影响其他请求的处理
- **容错性**：异常时记录日志，不影响流的传输

---

### 🚀 使用示例

#### 1. 流式生成代码

**后端调用**：
```java
@Resource
private AiCodeGeneratorFacade aiCodeGeneratorFacade;

public void generateCodeStream() {
    String userMessage = "生成一个响应式的登录界面";
    CodeGenTypeEnum type = CodeGenTypeEnum.HTML;
    
    // 获取流式输出
    Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, type);
    
    // 订阅并处理
    codeStream.subscribe(
        chunk -> System.out.print(chunk),           // 实时输出每个代码片段
        error -> System.err.println("错误: " + error), // 错误处理
        () -> System.out.println("\n生成完成！")      // 完成回调
    );
}
```

#### 2. 测试流式接口

**单元测试**：
```java
@Test
void generateAndSaveCodeStream() {
    Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(
        "生成一个登录界面，尽可能使用20行以内的代码实现。", 
        CodeGenTypeEnum.HTML
    );
    
    // 收集所有代码片段
    List<String> result = codeStream.collectList().block();
    Assertions.assertNotNull(result);
    
    // 拼接完整代码
    String completeContent = String.join("", result);
    Assertions.assertNotNull(completeContent);
    System.out.println("生成的代码：\n" + completeContent);
}
```

#### 3. SSE 前端集成（预览）

**前端代码示例**：
```javascript
// 使用 EventSource 接收 SSE 流
const eventSource = new EventSource('/api/code/generate/stream?message=生成登录界面');

eventSource.onmessage = (event) => {
    // 实时显示生成的代码片段
    const chunk = event.data;
    document.getElementById('code-output').textContent += chunk;
};

eventSource.onerror = (error) => {
    console.error('SSE 错误:', error);
    eventSource.close();
};

eventSource.addEventListener('complete', () => {
    console.log('代码生成完成！');
    eventSource.close();
});
```

---

### 🐛 Bug 修复

#### 1. Flux 类型识别错误

**问题**：`Flux cannot be resolved to a type`

**原因**：缺少 `reactor.core.publisher.Flux` 导入

**修复**：
```java
// 添加导入
import reactor.core.publisher.Flux;
```

#### 2. LangChain4j Reactor 模块缺失

**问题**：`Please import langchain4j-reactor module if you wish to use Flux<String>`

**原因**：未添加 `langchain4j-reactor` 依赖

**修复**：
```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-reactor</artifactId>
    <version>1.1.0-beta7</version>
</dependency>
```

#### 3. 提示词文件路径错误

**问题**：`@SystemMessage's resource 'prompt/codegen-html-prompt.txt' not found`

**原因**：文件名拼写错误

**修复**：
```java
// ❌ 错误
@SystemMessage(fromResource = "prompt/codegen-html-prompt.txt")

// ✅ 正确
@SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
```

#### 4. Flux 多次订阅错误

**问题**：`Sinks.many().unicast() sinks only allow a single Subscriber`

**原因**：冷流被多次订阅

**修复**：
```java
// 使用 .cache() 转换为热流
return result.cache()
        .doOnNext(...)
        .doOnComplete(...);
```

#### 5. 流式模型 API Key 未配置

**问题**：`dev.langchain4j.exception.AuthenticationException`

**原因**：`streaming-chat-model` 的 `api-key` 使用了占位符

**修复**：
```yaml
streaming-chat-model:
  api-key: sk-xxxxx  # 使用真实的 API Key
```

#### 6. 正则表达式错误

**问题**：代码解析失败，无法提取代码块

**原因**：
- `[\\s\\s]` 应该是 `[\\s\\S]`（匹配任意字符）
- `\\s*\\s\\n` 应该是 `\\s*\\n`（简化空白匹配）

**修复**：
```java
// ✅ 正确的正则表达式
Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE)
```

---

### 📊 项目结构更新

**新增/修改的文件**：
```
YuCodeMother-Backend/
├── core/
│   ├── AiCodeGeneratorFacade.java           # 新增流式方法
│   └── CodeParser.java                      # 新增代码解析器
├── ai/
│   └── AiCodeGeneratorService.java          # 新增流式接口
├── test/
│   └── core/
│       ├── AiCodeGeneratorFacadeTest.java   # 新增流式测试
│       └── CodeParserTest.java              # 新增解析器测试
└── resources/
    └── application-local.yml                # 新增流式模型配置
```

---

### 📦 依赖版本

**新增依赖**：
- **reactor-core**: 3.7.17（Spring Boot 管理版本）
- **langchain4j-reactor**: 1.1.0-beta7

**完整依赖列表**：
- Spring Boot: 3.5.13
- Java: 21
- LangChain4j: 1.1.0
- LangChain4j OpenAI Starter: 1.1.0-beta7
- LangChain4j Reactor: 1.1.0-beta7
- Project Reactor: 3.7.17
- MyBatis-Flex: 1.11.6
- Lombok: 1.18.44
- Hutool: 5.8.44
- Knife4j: 4.4.0

---

### 🎯 核心优势

#### 1. 实时用户体验
- **逐字输出**：用户可以实时看到 AI 生成代码的过程
- **进度反馈**：不再需要等待完整结果，提升用户体验
- **类似 ChatGPT**：模仿主流 AI 产品的交互方式

#### 2. 高性能异步处理
- **非阻塞**：基于 Reactor 的响应式编程模型
- **背压支持**：自动处理生产者和消费者速度不匹配
- **资源高效**：不占用线程等待 AI 响应

#### 3. 灵活的架构设计
- **双模式支持**：流式和非流式可以并存
- **统一接口**：使用相同的门面模式
- **易于扩展**：可以轻松添加新的流式功能

#### 4. 智能代码处理
- **自动解析**：智能识别 Markdown 代码块
- **容错机制**：代码块缺失时使用完整内容
- **多语言支持**：支持 HTML、CSS、JavaScript

#### 5. 完善的测试覆盖
- **单元测试**：覆盖流式生成和代码解析
- **集成测试**：验证完整的流式处理流程
- **错误场景**：测试各种异常情况

---

### 🔮 未来规划

#### 短期目标（v0.6）
- ✅ **SSE Controller**：实现 REST API 端点支持 SSE
- ✅ **前端集成**：Vue 3 前端实时显示生成进度
- ✅ **进度条**：显示代码生成的百分比
- ✅ **取消功能**：允许用户中断生成过程

#### 中期目标（v0.7-v0.8）
- ✅ **WebSocket 支持**：提供 WebSocket 作为 SSE 的替代方案
- ✅ **流式解析**：边生成边解析，无需等待完整内容
- ✅ **增量保存**：实时保存生成的代码片段
- ✅ **错误重试**：流式传输失败时自动重试

#### 长期目标（v1.0）
- ✅ **多模型支持**：支持 OpenAI、Claude、Gemini 等多个模型
- ✅ **流式编辑**：支持对生成的代码进行流式修改
- ✅ **协作功能**：多用户实时查看同一个生成过程
- ✅ **性能优化**：优化流式传输的性能和稳定性

---

### 📝 使用建议

#### 1. 选择合适的模式

**非流式模式**：
- 适合后台批量生成
- 不需要实时反馈的场景
- 需要完整结果后再处理

**流式模式**：
- 适合前端交互场景
- 需要实时反馈的场景
- 提升用户体验的场景

#### 2. 错误处理

**流式错误处理**：
```java
codeStream.subscribe(
    chunk -> handleChunk(chunk),
    error -> {
        log.error("流式生成失败", error);
        // 通知前端错误
        // 清理资源
    },
    () -> log.info("生成完成")
);
```

#### 3. 性能优化

**建议**：
- 使用 `.cache()` 避免重复订阅
- 合理设置 `max-tokens` 避免超时
- 监控 API 调用频率和成本
- 实现请求限流和防抖

#### 4. 安全考虑

**注意事项**：
- 验证用户输入，防止注入攻击
- 限制生成代码的大小
- 记录所有 API 调用日志
- 保护 API Key 不被泄露

---

### 👨‍💻 贡献者

- [@Karry178](https://github.com/Karry178) - 项目负责人

---

### 📄 许可证

本项目采用 MIT 许可证

---

**发布日期**: 2026-04-29  
**版本号**: v0.5  
**分支**: 0.5  
**核心功能**: AI 零代码生成应用 - SSE 流式输出
