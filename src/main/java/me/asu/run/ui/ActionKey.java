package me.asu.run.ui;

public enum ActionKey {

    GLOBAL_SEARCH(1),
    GLOBAL_Test(2);

    int key;

    ActionKey(int k) {this.key = k;}
    public static ActionKey getByKey(int k) {
        ActionKey[] values = values();
        for (ActionKey value : values) {
            if (value.key == k) return value;
        }
        return null;
    }

    public int getKey() {
        return key;
    }
}