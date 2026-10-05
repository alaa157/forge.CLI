package com.forgeci.application.pipeline;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
class ContainerExecutorTest {
 @Test void rejectsUnsafeImage(){ContainerExecutor e=new ContainerExecutor("docker",10);assertThrows(IllegalArgumentException.class,()->e.execute("alpine;rm","echo ok","run","abc"));}
}
