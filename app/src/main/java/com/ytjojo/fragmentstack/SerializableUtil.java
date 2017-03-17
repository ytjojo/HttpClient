package com.ytjojo.fragmentstack;

import android.app.Activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class SerializableUtil {
    public static ArrayList<ActionState> get(Activity activity){
        String name = "fragmentStack"+activity.getClass().getName();
        String path =activity.getCacheDir().getAbsolutePath();
        String filePath =  path+File.pathSeparatorChar+name+"out";
        FileInputStream fos;
        ObjectInputStream out;
        try {
            fos = new FileInputStream(new File(path));
            out = new ObjectInputStream(fos);
            ArrayList<ActionState> actionState=(ArrayList<ActionState>) out.readObject();
            fos.close();
            out.close();
            return actionState;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }
    public static void delete(Activity activity){
        String name = "fragmentStack"+activity.getClass().getName();
        String path =activity.getCacheDir().getAbsolutePath();
        String filePath =  path+File.pathSeparatorChar+name+"out";
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
        }

    }
    public static void save(Activity activity ,ArrayList<ActionState> actionStates){
        String name = "fragmentStack"+activity.getClass().getName();
        String path =activity.getCacheDir().getAbsolutePath();
        String filePath =  path+File.pathSeparatorChar+name+"out";
        FileOutputStream fos;
        ObjectOutputStream out;
        try {
            fos = new FileOutputStream(new File(path));
            out = new ObjectOutputStream(fos);
            out.writeObject(actionStates);
            fos.flush();
            out.flush();
            fos.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
        }

    }
}
