package org.dchbx.coveragehq.statemachine;

public interface EventProcessor {
    StateManager.AppEvents process(StateManager.AppEvents e) throws Exception;
}
