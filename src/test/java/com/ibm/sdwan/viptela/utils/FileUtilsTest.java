package com.ibm.sdwan.viptela.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class FileUtilsTest {
	
	@Test
	@DisplayName("Testing positive scenario for LifecycleScripts for filename and entry are same")
	public void getFileFromLifecycleScriptsTest() {
		String expected="{\n"
				+ "  \"enterpriseId\": {{ enterpriseId }},\n"
				+ "  \"id\": {{ id }}\n"
				+ "}";
		String result =FileUtils.getFileFromLifecycleScripts("UEsDBBQACAgIAAAAIQAAAAAAAAAAAAAAAAAZABEAdGVtcGxhdGVzL0RlbGV0ZUVkZ2UuanNvblVUDQAHAAAAAAAAAAAAAAAAq+ZSUFBKzStJLSooyixO9UxRslKorlZAFlGordUBqcqEymWCRLhqAVBLBwhH2pcgKAAAADoAAABQSwMEFAAICAgAAAAhAAAAAAAAAAAAAAAAABwAEQB0ZW1wbGF0ZXMvRWRnZVByb3Zpc2lvbi5qc29uVVQNAAcAAAAAAAAAAAAAAABdzkEKwyAQheF9TiGue4LuWyiU3GESX4OgY1HTUoJ3jxYNtkv/+ZhxG4SQ4Aj/9DrgpuRZbJvoi0jpVNTs+KGX1VPUjhv8i82SgY/hwjQZFCkz/WkZyq+0TsGMq53gq+vKoaAW3PUM7r7Yp3ZX4ZXTlaw2n7quT8c+Jos6D+pNPOZ3GQ5pB1BLBwhYrn36hgAAABEBAABQSwECFAAUAAgICAAAACEAR9qXICgAAAA6AAAAGQAJAAAAAAAAAAAAAAAAAAAAdGVtcGxhdGVzL0RlbGV0ZUVkZ2UuanNvblVUBQAHAAAAAFBLAQIUABQACAgIAAAAIQBYrn36hgAAABEBAAAcAAkAAAAAAAAAAAAAAIAAAAB0ZW1wbGF0ZXMvRWRnZVByb3Zpc2lvbi5qc29uVVQFAAcAAAAAUEsFBgAAAAACAAIAowAAAGEBAAAAAA==", 
				 "templates/DeleteEdge.json");
		Assertions.assertNotNull(result);
		Assertions.assertEquals(expected, result);
		
		
	}
	
	@Test
	@DisplayName("Testing negative scenario for LifecycleScripts for filename and entry are not same")
	public void getFileFromLifecycleScriptsTest1() {
		String result =FileUtils.getFileFromLifecycleScripts("UEsDBBQACAgIAAAAIQAAAAAAAAAAAAAAAAAZABEAdGVtcGxhdGVzL0RlbGV0ZUVkZ2UuanNvblVUDQAHAAAAAAAAAAAAAAAAq+ZSUFBKzStJLSooyixO9UxRslKorlZAFlGordUBqcqEymWCRLhqAVBLBwhH2pcgKAAAADoAAABQSwMEFAAICAgAAAAhAAAAAAAAAAAAAAAAABwAEQB0ZW1wbGF0ZXMvRWRnZVByb3Zpc2lvbi5qc29uVVQNAAcAAAAAAAAAAAAAAABdzkEKwyAQheF9TiGue4LuWyiU3GESX4OgY1HTUoJ3jxYNtkv/+ZhxG4SQ4Aj/9DrgpuRZbJvoi0jpVNTs+KGX1VPUjhv8i82SgY/hwjQZFCkz/WkZyq+0TsGMq53gq+vKoaAW3PUM7r7Yp3ZX4ZXTlaw2n7quT8c+Jos6D+pNPOZ3GQ5pB1BLBwhYrn36hgAAABEBAABQSwECFAAUAAgICAAAACEAR9qXICgAAAA6AAAAGQAJAAAAAAAAAAAAAAAAAAAAdGVtcGxhdGVzL0RlbGV0ZUVkZ2UuanNvblVUBQAHAAAAAFBLAQIUABQACAgIAAAAIQBYrn36hgAAABEBAAAcAAkAAAAAAAAAAAAAAIAAAAB0ZW1wbGF0ZXMvRWRnZVByb3Zpc2lvbi5qc29uVVQFAAcAAAAAUEsFBgAAAAACAAIAowAAAGEBAAAAAA==", 
				 "templates/test.json");
		Assertions.assertNull(result);
		
		
	}

}
