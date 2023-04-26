package it.govhub.govio.planner.batch.step;

import java.time.OffsetDateTime;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;

	public class LookForLastDateTasklet implements Tasklet {
		@Value("${planner.ntfy.min-last-date}")
		private String epoch;

		@Autowired
		GovioFileProducedRepository govioFileProducedRepository;
		
		@Override
		public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			OffsetDateTime date = govioFileProducedRepository.lastDateNotifyFile();
			ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
			jobExecutionContext.put("date",(date==null)? epoch: date.toString());
			return null;
		}
}
