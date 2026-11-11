package com.example.aicodehelper.ai.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class HealthTools {

    @Tool(name="calculateBMI", value="Calculate BMI based on height and weight.")
    public String calculateBMI(@P(value = "weight in kilograms,e.g.,55.5") double weight,
                               @P(value = "height in centimeters,e.g.,155.5") double height){
        if (weight <= 0 || height <= 0){
            return "输入的身高或者体重不合法，无法计算BMI。";
        }
        double heightM = height / 100.0;
        double bmi = weight / (heightM * heightM);
        String category;
        if(bmi < 18.5){
            category="偏瘦";
        }else if(bmi<24){
            category="正常";
        }else if(bmi<28){
            category="偏胖";
        }else{
            category="肥胖";
        }
        return String.format("用户的BMI指数为: %.2f, 属于【%s】范围", bmi, category);
    }
    @Tool(name="calculateBMR", value="Calculate Basal Metabolic Rate (BMR) using the Mifflin-St Jeor equation.")
    public String calculateBMR(@P(value = "gender, male or female") String gender,
                               @P(value = "age in years") int age,
                               @P(value = "height in centimeters") double height,
                               @P(value = "weight in kilograms") double weight) {
        if (weight <= 0 || height <= 0 || age <= 0) {
            return "输入的身高、体重或年龄不合法，无法计算基础代谢率。";
        }
        if (!"male".equalsIgnoreCase(gender) && !"男".equals(gender)
                && !"female".equalsIgnoreCase(gender) && !"女".equals(gender)) {
            return "性别参数不合法，请输入 male 或 female。";
        }
        return String.format("基础代谢率(BMR)约为 %.0f 千卡/天，即静息状态下每天维持生命所需的最低热量。", bmrValue(gender, age, height, weight));
    }

    @Tool(name="calculateDailyCalorieNeeds", value="Calculate total daily energy expenditure (TDEE) and recommended calorie intake for gentle weight loss.")
    public String calculateDailyCalorieNeeds(@P(value = "gender, male or female") String gender,
                                             @P(value = "age in years") int age,
                                             @P(value = "height in centimeters") double height,
                                             @P(value = "weight in kilograms") double weight,
                                             @P(value = "activity level: sedentary, light, moderate, active or very active") String activityLevel) {
        if (weight <= 0 || height <= 0 || age <= 0) {
            return "输入的身高、体重或年龄不合法，无法计算每日热量建议。";
        }
        String level = (activityLevel == null ? "" : activityLevel.trim().toLowerCase()).replaceAll("[ _-]", "");
        double factor;
        switch (level) {
            case "sedentary": factor = 1.2; break;
            case "light": factor = 1.375; break;
            case "moderate": factor = 1.55; break;
            case "active": factor = 1.725; break;
            case "veryactive": factor = 1.9; break;
            default: return "活动水平参数不合法，请输入 sedentary/light/moderate/active/very active。";
        }
        double tdee = bmrValue(gender, age, height, weight) * factor;
        double minIntake = Math.max(tdee - 500, 1200);
        double maxIntake = Math.max(tdee - 300, minIntake);
        return String.format("估算每日总消耗(TDEE)约 %.0f 千卡。温和减重建议每日摄入约 %.0f ~ %.0f 千卡（能量缺口 300~500 千卡，已确保不低于 1200 千卡）。", tdee, minIntake, maxIntake);
    }

    @Tool(name="calculateIdealWeightRange", value="Estimate healthy weight range based on BMI between 18.5 and 24.")
    public String calculateIdealWeightRange(@P(value = "height in centimeters") double height) {
        if (height <= 0) {
            return "输入的身高不合法，无法计算健康体重范围。";
        }
        double heightM = height / 100.0;
        double minWeight = 18.5 * heightM * heightM;
        double maxWeight = 24.0 * heightM * heightM;
        return String.format("按BMI 18.5~24.0估算，您的健康体重范围约为 %.1f ~ %.1f 公斤。", minWeight, maxWeight);
    }

    @Tool(name="calculateDailyProteinNeed", value="Estimate daily protein intake (1.2-1.6 g per kg body weight) for weight management.")
    public String calculateDailyProteinNeed(@P(value = "weight in kilograms") double weight) {
        if (weight <= 0) {
            return "输入的体重不合法，无法计算蛋白质需求。";
        }
        return String.format("减重期间建议每日蛋白质摄入约 %.0f ~ %.0f 克（按每公斤体重 1.2~1.6 克估算），有助于保护肌肉并增强饱腹感。", weight * 1.2, weight * 1.6);
    }

    @Tool(name="calculateDailyWaterIntake", value="Estimate daily water intake (30-35 ml per kg body weight).")
    public String calculateDailyWaterIntake(@P(value = "weight in kilograms") double weight) {
        if (weight <= 0) {
            return "输入的体重不合法，无法计算饮水量建议。";
        }
        return String.format("建议每日饮水约 %.0f ~ %.0f 毫升（按每公斤体重 30~35 毫升估算），可分多次饮用，运动或出汗多时适当增加。", weight * 30, weight * 35);
    }

    private double bmrValue(String gender, int age, double height, double weight) {
        boolean male = "male".equalsIgnoreCase(gender) || "男".equals(gender);
        return 10 * weight + 6.25 * height - 5 * age + (male ? 5 : -161);
    }
}
