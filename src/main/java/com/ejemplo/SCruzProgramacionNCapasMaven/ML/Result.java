package com.ejemplo.SCruzProgramacionNCapasMaven.ML;

import java.util.List;
import org.apache.poi.ss.formula.functions.T;

public class Result<T> {

    public boolean correct;
    public String errorMessage;
    public Exception ex;
    public T object;
    public List<T> objects;

    public List<T> getObjects() {
        return objects;
    }

    public void setObjects(List<T> objects) {
        this.objects = objects;
    }
    
    

}
