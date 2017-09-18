package com.assistant;

/**
 * Created by virtualC9 on 2017/9/16.
 */

public class TargetEntity {
    private String curDate; //当天的日期
    private String targetSteps;   //当天的步数

    public TargetEntity(){

    }
    public TargetEntity(String curDate,String targetSteps){
        this.curDate=curDate;
        this.targetSteps=targetSteps;
    }
    public String getCurDate(){
        return curDate;
    }
    public void setCurDate(String curData){
        this.curDate=curData;
    }
    public String getTargetSteps(){
        return targetSteps;
    }
    public void setTargetSteps(String targetSteps){
        this.targetSteps=targetSteps;
    }

    @Override
    public String toString() {
        return "TargetEntity{" +
                "curDate='" + curDate + '\'' +
                ", targetSteps=" + targetSteps +
                '}';
    }
}
