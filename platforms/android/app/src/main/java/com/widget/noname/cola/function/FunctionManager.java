package com.widget.noname.cola.function;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.widget.noname.plus.common.function.BaseFunction;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class FunctionManager {

    private final ConcurrentHashMap<String, BaseFunction> functionMap = new ConcurrentHashMap<>();

    private final ViewGroup container;
    private final Context context;
    private BaseFunction currentFunction = null;

    public FunctionManager(Context context, ViewGroup container, List<FunctionBean> functions) {
        this.context = context;
        this.container = container;
        initFunctions(functions);
    }

    private void initFunctions(List<FunctionBean> functions) {
        functions.forEach(f -> {
            BaseFunction o = newInstance(f.getPath());

            if (o != null) {
                o.setContainer(container);
                functionMap.put(f.getName(), o);
            }
        });
    }

    private BaseFunction newInstance(String classname) {
        try {
            Class<?> clazz = Class.forName(classname);
            Constructor<?> constructor = clazz.getConstructor(Context.class);
            return (BaseFunction) constructor.newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onCreate() {
        functionMap.forEach((k, f) -> f.onCreate());
    }

    public void onPause() {
        functionMap.forEach((k, f) -> f.onPause());
    }

    public void onResume() {
        functionMap.forEach((k, f) -> f.onResume());
    }

    public boolean checkToSwitch(String functionName) {
        BaseFunction baseFunction = functionMap.get(functionName);

        if (null != baseFunction) {
            View view = baseFunction.obtainView();

            if (view != null) {
                Optional.ofNullable(currentFunction).ifPresent(BaseFunction::onRecycle);

                currentFunction = baseFunction;
                currentFunction.onCreate();
                replaceFunctionContainer(view);
                return true;
            }
        }

        return false;
    }

    private void replaceFunctionContainer(View view) {
        container.removeAllViews();
        container.addView(view);
    }

    public boolean onBackPressed() {
        if (null != currentFunction) {
            currentFunction.onRecycle();
            currentFunction = null;

            return true;
        }


        return false;
    }
}
