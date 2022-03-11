package com.mr.mf_pd.cpmpiler;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes({"com.mr.mf_pd.annotation.MyClass"})
public class TestProcess extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //JAVAC
        System.out.println("=====process====");
        if (set.isEmpty()){
            System.out.println("set.isEmpty -----process----");
        }else {
            System.out.println("set.isNotEmpty-----process----");
        }
        return false;
    }
}