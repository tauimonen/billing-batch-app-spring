package example.billingapp;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FilePreparationTasklet implements Tasklet {

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
        String inputFile = jobParameters.getString("input.file");

        if (inputFile != null) {
            Path source = Paths.get(inputFile);
            Path target = Paths.get("staging", source.toFile().getName());

            // Ensure the source file exists before copying
            if (Files.exists(source)) {
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("File " + source + " copied to: " + target.toAbsolutePath());
                return RepeatStatus.FINISHED;
            } else {
                throw new IllegalArgumentException("Source file does not exist: " + inputFile);
            }
        } else {
            throw new IllegalArgumentException("Input file is null");
        }
    }
}
