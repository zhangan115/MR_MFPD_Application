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
        System.out.println("====");
        return false;
    }
}