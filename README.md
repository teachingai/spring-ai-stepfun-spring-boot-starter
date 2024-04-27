# spring-ai-stepfun-spring-boot-starter

> 基于 [阶跃星辰](https://platform.stepfun.com/docs/Chat/chat-completion-create) 的 ChatGLM 模型 和 Spring AI 的 Spring Boot Starter 实现

### 阶跃星辰

阶跃星辰 开放平台提供一系列具有不同功能和定价的大模型，包括通用大模型、超拟人大模型、图像大模型、向量大模型等，并且支持使用您的私有数据对模型进行微调。

- 官网地址：[https://www.stepfun.com)
- API文档：[https://platform.stepfun.com/docs/Chat/chat-completion-create)

#### 关键概念

##### GLM

> GLM 全名 General Language Model ，是一款基于自回归填空的预训练语言模型。ChatGLM 系列模型，支持相对复杂的自然语言指令，并且能够解决困难的推理类问题。该模型配备了易于使用的 API 接口，允许开发者轻松将其融入各类应用，广泛应用于智能客服、虚拟主播、聊天机器人等诸多领域。

##### Token

> Token 是模型用来表示自然语言文本的基本单位，可以直观的理解为“字”或“词”；通常 1 个中文词语、1 个英文单词、1 个数字或 1 个符号计为 1 个token。 一般情况下 ChatGLM 系列模型中 token 和字数的换算比例约为 1:1.6 ，但因为不同模型的分词不同，所以换算比例也存在差异，每一次实际处理 token 数量以模型返回为准，您可以从返回结果的 usage 中查看。

#### 支持的功能包括：

- 支持文本生成（Chat Completion API）
- 支持多轮对话（Chat Completion API），支持返回流式输出结果
- 支持图文混合描述
#### 资源

- 查看模型[接口文档](https://platform.stepfun.com/docs/Chat/chat-completion-create)
- 体验模型能力[体验中心](https://stepchat.cn/chats/new)

#### 模型

阶跃星辰 开放平台提供了包括通用大模型、图像大模型、超拟人大模型、向量大模型等多种模型。

| 模型 | 描述                                                                                                                     |
| ------------ |------------------------------------------------------------------------------------------------------------------------|
| Step-1  | 千亿参数语言大模型，在逻辑推理、中文知识、英文知识、数学、代码等方面的性能全面超过GPT-3.5                                                                       |
| Step-1V  | 千亿参数多模态大模型，在图像理解、多轮指令跟随、数学能力、逻辑推理、文本创作等方面性能达到业界领先水平。它还具有出色的多模理解能力，可以精准描述和理解图像中的文字、数据、图表等信息，并根据图像信息实现内容创作、逻辑推理、数据分析等任务。 |
| Step-2  | 万亿参数MoE语言大模型预览版，采用MoE架构，聚焦于深度智能的探索。该模型目前仅提供API接口给部分合作伙伴试用。                                                             | |


### Maven

``` xml
<dependency>
	<groupId>com.github.teachingai</groupId>
	<artifactId>spring-ai-stepfun-spring-boot-starter</artifactId>
	<version>${project.version}</version>
</dependency>
```

### Sample

使用示例请参见 [Spring AI Examples](https://github.com/TeachingAI/spring-ai-examples)


### License

[Apache License 2.0](LICENSE)
