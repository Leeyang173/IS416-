package sg.edu.smu.livelabs.mobicom.services;

import android.os.Bundle;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Stack;

import mortar.ViewPresenter;
import sg.edu.smu.livelabs.mobicom.App;

/**
 * Created by Aftershock PC on 25/7/2015.
 */
public class ScreenService {
    private HashMap<String, Stack<Serializable>> stacks;

    public ScreenService() {
        stacks = new HashMap<>();
    }

    public void push(Class<? extends ViewPresenter> pushPresenter,  Serializable data) {
        Stack<Serializable> stack = getStack(pushPresenter.getCanonicalName(), true);
        stack.push(data);
    }

    public <T extends Serializable> T pop(Class<? extends ViewPresenter> pushPresenter) {
        Stack<Serializable> stack = getStack(pushPresenter.getCanonicalName(), false);
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return (T) stack.pop();
    }

    public void clearStack(Class<? extends ViewPresenter> pushPresenter){
        Stack<Serializable> stack = getStack(pushPresenter.getCanonicalName(), false);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        stack.clear();
    }

    public void clearAll(){
        try {
            for (Stack stack: stacks.values()) {
                stack.clear();
            }
        } catch (Exception e){
            Log.e(App.APP_TAG, "ScreenServices.clearAll()", e);
        }

    }

    public void save(Bundle b) {
        b.putSerializable("_ScreenServiceStacks", stacks);
    }

    public void restore(Bundle b) {
        if (b != null) {
            stacks = (HashMap<String, Stack<Serializable>>) b.getSerializable("_ScreenServiceStacks");
        } else {
            stacks = new HashMap<>();
        }
    }

    private Stack<Serializable> getStack(String stackName, boolean create) {
        Stack<Serializable> stack = stacks.get(stackName);
        if (stack == null && create) {
            stack = new Stack<>();
            stacks.put(stackName, stack);
        }
        return stack;
    }
}
