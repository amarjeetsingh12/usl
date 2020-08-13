package com.flipkart.gap.usl.core.processor.stage;

import com.codahale.metrics.Timer;
import com.flipkart.gap.usl.core.metric.JmxReporterMetricRegistry;
import com.flipkart.gap.usl.core.processor.stage.model.ProcessingStageData;

public abstract class ProcessingStage {
    abstract protected void process(ProcessingStageData request) throws StageProcessingException;

    public final void execute(ProcessingStageData processingStageData) throws StageProcessingException {
        Timer.Context context = JmxReporterMetricRegistry.getMetricRegistry().timer(this.getClass().getSimpleName()).time();
        JmxReporterMetricRegistry.getInstance().updateGroupedSize(this.getClass().getCanonicalName(), processingStageData.getDimensionMutateRequests().size());
        try {
            this.process(processingStageData);
        } catch (StageProcessingException spe) {
            throw spe;
        } catch (Throwable e) {
            throw new StageProcessingException(e);
        } finally {
            context.stop();
        }
    }

}
