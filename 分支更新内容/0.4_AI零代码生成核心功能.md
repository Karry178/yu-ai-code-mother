🎉 **YuCodeMother v0.4 版本发布说明**

### 📋 版本概述

本次 v0.4 版本是项目的核心功能版本，实现了基于 AI 的零代码生成应用系统。通过集成 LangChain4j 框架和 DeepSeek 大模型，用户只需输入自然语言描述，即可自动生成完整的前端网页代码，真正实现"零代码"开发体验。

---

### ✨ 新增功能

#### 🤖 AI 代码生成核心功能

**1. 双模式代码生成**
- ✅ **HTML 单文件模式**：生成包含所有代码的独立 HTML 文件
- ✅ **多文件分离模式**：生成结构化的 HTML + CSS + JS 三文件项目
- ✅ 支持响应式设计，自动适配桌面和移动端
- ✅ 纯原生代码实现，无外部依赖
- ✅ 智能占位符填充（文本、图片）

**2. 智能提示词工程**
- ✅ 专业的系统提示词模板（System Prompt）
- ✅ 结构化输出约束（JSON Schema）
- ✅ 代码质量保证（注释、规范、安全性）
- ✅ 交互功能支持（Tab 切换、轮播图、表单验证等）

**3. 文件管理系统**
- ✅ 自动创建唯一目录（雪花 ID）
- ✅ 智能文件保存策略
- ✅ 支持多文件项目结构
- ✅ 本地文件系统持久化

#### 🏗️ 技术架构设计

**核心设计模式**

**1. 门面模式（Facade Pattern）**
通过 `AiCodeGeneratorFacade` 统一代码生成入口，简化客户端调用：

```java
@Service
public class AiCodeGeneratorFacade {
    
    @Resource
    AiCodeGeneratorService aiCodeGeneratorService;
    
    /**
     * 统一入口：根据类型生成并保存代码
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        // 校验 + 路由 + 生成 + 保存
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型");
        };
    }
}
```

**设计优势**：
- 隐藏复杂的 AI 调用和文件保存逻辑
- 统一异常处理和参数校验
- 易于扩展新的生成模式

**2. 工厂模式（Factory Pattern）**
通过 `AiCodeGeneratorServiceFactory` 创建 AI 服务实例：

```java
@Configuration
public class AiCodeGeneratorServiceFactory {
    
    @Resource
    private ChatModel chatModel;
    
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return AiServices.create(AiCodeGeneratorService.class, chatModel);
    }
}
```

**设计优势**：
- 解耦 AI 服务创建逻辑
- 支持依赖注入和 Spring 容器管理
- 便于切换不同的 AI 模型

**3. 策略模式（Strategy Pattern）**
通过枚举类型 `CodeGenTypeEnum` 实现不同生成策略：

```java
@Getter
public enum CodeGenTypeEnum {
    HTML("原生 HTML 模式", "html"),
    MULTI_FILE("原生多文件模式", "multi_file");
    
    private final String text;
    private final String value;
}
```

**设计优势**：
- 类型安全的策略选择
- 易于添加新的生成模式
- 配合 Java 21 的 switch 表达式实现优雅路由

**核心技术栈**

**LangChain4j 集成**
- **版本**：1.1.0 + 1.1.0-beta7（OpenAI Starter）
- **模型**：DeepSeek Chat（兼容 OpenAI API）
- **核心特性**：
  - 结构化输出（Structured Output）
  - 系统提示词注解（@SystemMessage）
  - 自动 JSON 解析
  - 请求/响应日志

**DeepSeek API 配置**
```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://api.deepseek.com
      api-key: sk-xxxxx
      model-name: deepseek-chat
      max-tokens: 8192
      strict-json-schema: true
      response-format: json_object
      log-requests: true
      log-responses: true
```

**配置说明**：
- `max-tokens: 8192`：确保生成完整代码
- `strict-json-schema: true`：强制结构化输出
- `response-format: json_object`：DeepSeek 特有配置

#### 📦 数据模型设计

**1. HTML 单文件结果模型**
```java
@Description("生成HTML代码文件的结果")
@Data
public class HtmlCodeResult {
    
    @Description("HTML代码")
    private String htmlCode;
    
    @Description("生成代码的描述")
    private String description;
}
```

**2. 多文件结果模型**
```java
@Description("生成多个代码文件的结果")
@Data
public class MultiFileCodeResult {
    
    @Description("HTML代码")
    private String htmlCode;
    
    @Description("CSS代码")
    private String cssCode;
    
    @Description("JS代码")
    private String jsCode;
    
    @Description("生成代码的描述")
    private String description;
}
```

**设计亮点**：
- 使用 `@Description` 注解指导 AI 输出
- LangChain4j 自动将 JSON 映射到 Java 对象
- 类型安全的结构化输出

#### 🎯 提示词工程（Prompt Engineering）

**HTML 单文件模式提示词**
```
你是一位资深的 Web 前端开发专家，精通 HTML、CSS 和原生 JavaScript。

约束:
1. 技术栈: 只能使用 HTML、CSS 和原生 JavaScript
2. 禁止外部依赖: 不允许使用任何外部框架或库
3. 独立文件: CSS 内联在 <style>，JS 放在 <script>
4. 响应式设计: 使用 Flexbox 或 Grid 布局
5. 内容填充: 使用 Lorem Ipsum 和 https://picsum.photos
6. 代码质量: 结构清晰、有注释、易维护
7. 交互性: 使用原生 JavaScript 实现
8. 安全性: 纯客户端代码，无服务器逻辑
```

**多文件模式提示词**
```
你是一位资深的 Web 前端开发专家，遵循代码分离和模块化的最佳实践。

约束:
1. 文件分离:
   - index.html: 只包含结构和内容
   - style.css: 所有样式规则
   - script.js: 所有交互逻辑
2. 引用方式:
   - <link rel="stylesheet" href="style.css">
   - <script src="script.js"></script>
3. 其他约束同单文件模式
```

**提示词设计原则**：
- 明确角色定位（资深前端专家）
- 详细的技术约束（禁止外部依赖）
- 清晰的输出格式要求
- 代码质量标准（注释、规范）

#### 💾 文件保存策略

**核心实现：CodeFileSaver**
```java
public class CodeFileSaver {
    
    // 保存根目录
    private static final String FILE_SAVE_ROOT_DIR = 
        System.getProperty("user.dir") + "/tmp/code_output";
    
    /**
     * 保存 HTML 单文件
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(baseDirPath);
    }
    
    /**
     * 保存多文件项目
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String baseDirPath = buildUniqueDir(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", multiFileCodeResult.getHtmlCode());
        writeToFile(baseDirPath, "style.css", multiFileCodeResult.getCssCode());
        writeToFile(baseDirPath, "script.js", multiFileCodeResult.getJsCode());
        return new File(baseDirPath);
    }
    
    /**
     * 构建唯一目录：业务类型_雪花ID
     */
    private static String buildUniqueDir(String bizType) {
        String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_ROOT_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }
}
```

**设计特点**：
- 雪花 ID 保证目录唯一性
- 业务类型前缀便于分类管理
- 统一的文件写入方法
- UTF-8 编码保证中文支持

**目录结构示例**
```
tmp/code_output/
├── html_1234567890123456789/
│   └── index.html
└── multi_file_9876543210987654321/
    ├── index.html
    ├── style.css
    └── script.js
```

#### 🔧 AI 服务接口设计

```java
public interface AiCodeGeneratorService {
    
    /**
     * 生成 HTML 代码
     * @param userMessage 用户提示词
     * @return AI 输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);
    
    /**
     * 生成多文件代码
     * @param userMessage 用户提示词
     * @return AI 输出结果
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
```

**设计亮点**：
- 接口方法对应不同生成模式
- `@SystemMessage` 注解加载提示词模板
- 返回类型自动触发结构化输出
- 无需手动解析 JSON

---

### 🔧 技术亮点

**1. LangChain4j 结构化输出**
通过 `@Description` 注解指导 AI 生成符合 Java 对象结构的 JSON：

```java
@Description("生成HTML代码文件的结果")
public class HtmlCodeResult {
    @Description("HTML代码")
    private String htmlCode;
}
```

AI 自动生成：
```json
{
  "htmlCode": "<!DOCTYPE html>...",
  "description": "一个响应式的个人博客页面"
}
```

**2. Java 21 Switch 表达式**
使用现代 Java 语法实现优雅的策略路由：

```java
return switch (codeGenTypeEnum) {
    case HTML -> generateAndSaveHtmlCode(userMessage);
    case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
    default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的生成类型");
};
```

**3. 雪花 ID 生成器**
使用 Hutool 工具库生成分布式唯一 ID：

```java
String uniqueDirName = StrUtil.format("{}_{}", bizType, IdUtil.getSnowflakeNextIdStr());
// 输出: html_1234567890123456789
```

**4. 系统提示词外部化**
将提示词存储在 `.txt` 文件中，便于维护和版本控制：

```
src/main/resources/prompt/
├── codegen-html-system-prompt.txt
└── codegen-multi-file-system-prompt.txt
```

---

### 📊 项目结构

**后端新增模块**
```
YuCodeMother-Backend/
├── ai/                          # AI 代码生成模块
│   ├── AiCodeGeneratorService.java          # AI 服务接口
│   ├── AiCodeGeneratorServiceFactory.java   # AI 服务工厂
│   └── model/                               # AI 输出模型
│       ├── HtmlCodeResult.java              # HTML 结果
│       └── MultiFileCodeResult.java         # 多文件结果
├── core/                        # 核心业务模块
│   ├── AiCodeGeneratorFacade.java           # 门面类
│   └── CodeFileSaver.java                   # 文件保存器
├── model/enums/
│   └── CodeGenTypeEnum.java                 # 生成类型枚举
└── resources/
    └── prompt/                              # 提示词模板
        ├── codegen-html-system-prompt.txt
        └── codegen-multi-file-system-prompt.txt
```

---

### 🚀 使用示例

**1. 调用门面类生成代码**
```java
@Resource
private AiCodeGeneratorFacade aiCodeGeneratorFacade;

public void generateCode() {
    String userMessage = "生成一个响应式的个人博客首页，包含导航栏、文章列表和页脚";
    CodeGenTypeEnum type = CodeGenTypeEnum.MULTI_FILE;
    
    File outputDir = aiCodeGeneratorFacade.generateAndSaveCode(userMessage, type);
    System.out.println("代码已保存到: " + outputDir.getAbsolutePath());
}
```

**2. 生成结果示例**
**用户输入**：
```
生成一个简单的待办事项列表应用，支持添加、删除和标记完成功能
```

**AI 输出**（多文件模式）：
- `index.html`：包含待办列表的 HTML 结构
- `style.css`：美观的样式设计（卡片布局、动画效果）
- `script.js`：完整的交互逻辑（增删改查、本地存储）

---

### 🐛 Bug 修复

**1. 枚举类语法错误**
**问题**：枚举常量分隔符错误
```java
// ❌ 错误写法
HTML("原生 HTML 模式", "html");  // 第一个常量用分号
MULTI_FILE("原生多文件模式", "multi_file");
```

**修复**：
```java
// ✅ 正确写法
HTML("原生 HTML 模式", "html"),  // 用逗号
MULTI_FILE("原生多文件模式", "multi_file");  // 最后一个用分号
```

**2. LangChain4j 配置警告**
**问题**：IDE 提示 "Unknown property 'langchain4j'"

**原因**：
- Spring Boot 配置元数据缺失
- 这是 IDE 级别的警告，不影响运行

**解决方案**：
- 忽略警告（运行时配置正常生效）
- 或添加 `spring-boot-configuration-processor` 依赖

**3. YAML 配置缩进错误**
**问题**：`api-key` 配置为对象而非字符串
```yaml
# ❌ 错误写法
api-key:
  model-name: deepseek-chat
```

**修复**：
```yaml
# ✅ 正确写法
api-key: sk-xxxxx
model-name: deepseek-chat
```

---

### 📦 依赖版本

**新增核心依赖**
- **LangChain4j**: 1.1.0
- **LangChain4j OpenAI Starter**: 1.1.0-beta7
- **Hutool**: 5.8.44（文件操作、雪花 ID）

**完整依赖列表**
- Spring Boot: 3.5.13
- Java: 21
- MyBatis-Flex: 1.11.6
- Lombok: 1.18.44
- Knife4j: 4.4.0
- MySQL Connector: 8.x
- HikariCP: 4.0.3

---

### 🎯 核心优势

**1. 零代码开发体验**
- 用户只需输入自然语言描述
- AI 自动生成完整可运行的前端代码
- 无需编写任何代码

**2. 高质量代码输出**
- 遵循前端开发最佳实践
- 响应式设计，适配多端
- 代码结构清晰，注释完善
- 纯原生实现，无外部依赖

**3. 灵活的生成模式**
- 单文件模式：快速原型开发
- 多文件模式：结构化项目开发
- 易于扩展新的生成模式

**4. 优雅的架构设计**
- 门面模式简化调用
- 工厂模式解耦创建
- 策略模式支持扩展
- 提示词外部化便于维护

**5. 企业级代码质量**
- 统一异常处理
- 参数校验完善
- 日志记录详细
- 文件管理规范

---

### 🔮 未来规划

**短期目标（v0.5）**
- ✅ 添加 React/Vue 组件生成模式
- ✅ 支持代码预览和在线编辑
- ✅ 实现代码版本管理
- ✅ 添加代码质量评分

**中期目标（v0.6-v0.7）**
- ✅ 支持后端代码生成（Spring Boot）
- ✅ 数据库表结构自动生成
- ✅ API 接口自动生成
- ✅ 前后端联调代码生成

**长期目标（v1.0）**
- ✅ 完整的全栈应用生成
- ✅ 微服务架构代码生成
- ✅ 自动化测试代码生成
- ✅ 部署脚本自动生成

---

### 📝 使用建议

**1. 提示词编写技巧**
- 描述要具体明确（功能、样式、交互）
- 提供参考示例或网站链接
- 说明目标用户和使用场景
- 指定特殊需求（颜色、布局、动画）

**2. 生成模式选择**
- **单文件模式**：适合简单页面、快速原型
- **多文件模式**：适合复杂项目、团队协作

**3. 代码优化建议**
- 生成后可手动调整样式细节
- 根据实际需求添加业务逻辑
- 集成后端 API 实现数据交互
- 添加错误处理和边界情况

---

### 👨‍💻 贡献者

- [@Karry178](https://github.com/Karry178) - 项目负责人

---

### 📄 许可证

本项目采用 MIT 许可证

---

**发布日期**: 2026-04-29  
**版本号**: v0.4  
**分支**: 0.4  
**核心功能**: AI 零代码生成应用
