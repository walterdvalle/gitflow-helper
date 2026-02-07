package br.com.gitflowhelper.actions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionParamsService {

    private static final ActionParamsService INSTANCE = new ActionParamsService();

    private final Map<String, ActionParams> storage = new ConcurrentHashMap<>();

    private ActionParamsService() { }

    public static ActionParamsService getInstance() {
        return INSTANCE;
    }

    public void put(String key, ActionParams value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("Key e value não podem ser null");
        }
        storage.put(key, value);
    }

    public ActionParams get(String key) {
        if (key == null) {
            return null;
        }
        return storage.get(key);
    }

    public void clear() {
        storage.clear();
    }
}
