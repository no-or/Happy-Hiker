package com.example.android.happyhiker3;

import java.util.HashMap;
import java.util.Map;

public class UserForm {
    public Integer key;
    public Integer height;
    public Integer weight;
    public Integer age;
    public String medicalConditions;
    public Map<String, Boolean> stars = new HashMap<>();

    public UserForm(){};

    public UserForm(Integer key, Integer height, Integer weight, Integer age, String medCond){
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.key = key;
        this.medicalConditions = medCond;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("height", height);
        result.put("weight", weight);
        result.put("age", age);
        result.put("medicalConditions", medicalConditions);

        return result;
    }

}
