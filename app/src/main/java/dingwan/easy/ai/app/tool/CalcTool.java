package dingwan.easy.ai.app.tool;

import dingwan.easy.ai.tool.annotation.AiParam;
import dingwan.easy.ai.tool.annotation.AiTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CalcTool {

    @AiTool(name = "add", description = "加法运算，返回两个数的和")
    public double add(
            @AiParam("第一个加数") double a,
            @AiParam("第二个加数") double b) {
        log.info("加法: {} + {} = {}", a, b, a + b);
        return a + b;
    }

    @AiTool(name = "subtract", description = "减法运算，返回第一个数减去第二个数的差")
    public double subtract(
            @AiParam("被减数") double a,
            @AiParam("减数") double b) {
        log.info("减法: {} - {} = {}", a, b, a - b);
        return a - b;
    }

    @AiTool(name = "multiply", description = "乘法运算，返回两个数的乘积")
    public double multiply(
            @AiParam("第一个乘数") double a,
            @AiParam("第二个乘数") double b) {
        log.info("乘法: {} * {} = {}", a, b, a * b);
        return a * b;
    }

    @AiTool(name = "divide", description = "除法运算，返回第一个数除以第二个数的商，除数不能为0")
    public double divide(
            @AiParam("被除数") double a,
            @AiParam("除数（不能为0）") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("除数不能为0");
        }
        log.info("除法: {} / {} = {}", a, b, a / b);
        return a / b;
    }

    @AiTool(name = "power", description = "幂运算，返回底数的指数次方")
    public double power(
            @AiParam("底数") double base,
            @AiParam("指数") double exponent) {
        log.info("幂运算: {} ^ {} = {}", base, exponent, Math.pow(base, exponent));
        return Math.pow(base, exponent);
    }

    @AiTool(name = "sqrt", description = "平方根运算，返回非负数的平方根")
    public double sqrt(
            @AiParam("要求平方根的非负数") double a) {
        if (a < 0) {
            throw new IllegalArgumentException("不能对负数求平方根");
        }
        log.info("平方根: sqrt({}) = {}", a, Math.sqrt(a));
        return Math.sqrt(a);
    }

    @AiTool(name = "modulo", description = "取模运算，返回第一个数除以第二个数的余数")
    public double modulo(
            @AiParam("被除数") double a,
            @AiParam("模数（不能为0）") double b) {
        if (b == 0) {
            throw new IllegalArgumentException("模数不能为0");
        }
        log.info("取模: {} % {} = {}", a, b, a % b);
        return a % b;
    }
}
