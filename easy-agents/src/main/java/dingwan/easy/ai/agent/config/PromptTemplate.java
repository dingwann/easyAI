package dingwan.easy.ai.agent.config;

public class PromptTemplate {

    public static String ReAct = """
            你是一个具备推理和行动能力的AI助手。你可以通过思考分析问题，然后调用合适的工具来获取信息，最终给出准确的答案。
            
            ## 可用工具
            {tools}
            
            ## 工作流程
            
            你需要先分析问题，然后决定：
            1. 调用工具获取信息
            2. 直接给出最终答案

            你的输出必须是合法JSON，且只能输出JSON，不允许输出任何解释文字。

            ## 输出格式
            ### 需要调用工具时
            {
              "thought": "你的分析过程",
              "tool_calls": [
                {
                  "name": "工具名称",
                  "arguments": {
                    "参数名": "参数值"
                  }
                }
              ]
            }
            ### 已经能够回答时
            {
              "thought": "你的分析过程",
              "final_answer": "最终答案"
            }
            
            ## 规则
            1. 必须返回合法JSON
            2. 不允许输出额外解释
            3. 可以一次调用多个工具
            4. 不要编造工具执行结果
            5. 工具结果会出现在执行历史中
            6. 如果工具结果不足，请继续调用工具
            7. 只有确定答案时才能返回final_answer
            
            ## 当前任务
            **Question:** {question}
            
            ## 执行历史
            {history}
            
            现在开始你的推理和行动:
            """;

    public static String Reflection_init = """
            请根据以下要求完成任务:
            任务: {task}
            请提供一个完整、准确的回答。
            """;

    public static String Reflection_reflect = """
            请仔细审查以下回答，并找出可能的问题或改进空间:
            # 原始任务:
            {task}
            # 当前回答:
            {content}
            请分析这个回答的质量，指出不足之处，并提出具体的改进建议。
            如果回答已经很好，请回答"无需改进"。
            如果提了改进建议就禁止出现"无需改进"四个字，改进应适可而止，只要不存在明显错误或者必要的优化没做时就不需要改进。
            """;

    public static String Reflection_refine = """
            请根据反馈意见改进你的回答:
            # 原始任务:
            {task}
            # 上一轮回答:
            {last_attempt}
            # 反馈意见:
            {feedback}
            请提供一个改进后的回答。
            """;
}
