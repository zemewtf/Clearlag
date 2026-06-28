package me.minebuilders.clearlag.modules;

import me.minebuilders.clearlag.SchedulerUtil;

public abstract class TaskModule extends ClearlagModule implements Runnable {

    protected SchedulerUtil.TaskRef taskRef = null;

    @Override
    public void setEnabled() {
        super.setEnabled();

        taskRef = startTaskRef();
    }

    protected SchedulerUtil.TaskRef startTaskRef() {
        return SchedulerUtil.scheduleRepeatingGlobal(this, getInterval(), getInterval());
    }

    @Override
    public void setDisabled() {
        super.setDisabled();
        if (taskRef != null) {
            taskRef.cancel();
            taskRef = null;
        }
    }

    public int getInterval() {
        return 20;
    }

}
