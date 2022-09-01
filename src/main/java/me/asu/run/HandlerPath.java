package me.asu.run;




import lombok.Getter;
import lombok.ToString;


import java.util.HashSet;
import java.util.Set;


@Getter
@ToString
public class HandlerPath {

    static class InstanceHolder {
       static HandlerPath instance =  new HandlerPath();
    }

    private Set<String> includePath = new HashSet<>();

    private Set<String> excludepath = new HashSet<>();

    private HandlerPath(){}

    public void addIncludePath(String path){
        this.includePath.add(path);
    }

    public void addExcludePath(String path){
        this.excludepath.add(path);
    }

    public static HandlerPath getInstance(){
        return InstanceHolder.instance;
    }
}
