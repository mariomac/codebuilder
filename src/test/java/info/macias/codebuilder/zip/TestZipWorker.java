package info.macias.codebuilder.zip;

import info.macias.kutils.FileUtilsKt;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Created by mmacias on 15/4/16.
 */
@RunWith(VertxUnitRunner.class)
public class TestZipWorker {
    private Vertx vertx;
    private static final String TMP_DIR = "/tmp/compiler";
    private static final String UPL_DIR = TMP_DIR + "/file-uploads";
    private static final String UNCOMP_DIR = TMP_DIR + "/uncompress";
    private static final String ADDRESS = "zipworker";

    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
     * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
     * completed its start sequence (thanks to `context.asyncAssertSuccess`).
     *
     * @param context the test context.
     */
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();
        new File(TMP_DIR).mkdirs();
        new File(UPL_DIR).mkdirs();
        new File(UNCOMP_DIR).mkdirs();
        DeploymentOptions options = new DeploymentOptions().setWorker(true);
        // We pass the options as the second parameter of the deployVerticle method.
        vertx.deployVerticle(new ZipWorkerVerticle(ADDRESS, UNCOMP_DIR), options, context.asyncAssertSuccess());
        String srcPath = getClass().getResource("/").toString().split(":")[1];
        FileUtilsKt.copy(srcPath + "test.zip", UPL_DIR + "/test.zip");
        FileUtilsKt.copy(srcPath + "wrong.zip", UPL_DIR + "/wrong.zip");
    }

    /**
     * This method, called after our test, just cleanup everything by closing the vert.x instance
     *
     * @param context the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Let's ensure that our application behaves correctly.
     *
     * @param context the test context
     */
    @Test
    public void testCorrectZip(TestContext context) {
        // This test is asynchronous, so get an async handler to inform the test when we are done.
        final Async async = context.async();
        vertx.eventBus().<JsonArray>send(ADDRESS, UPL_DIR + "/test.zip", replyHandler -> {
            if (replyHandler.succeeded()) {
                List files = replyHandler.result().body().getList();
                context.assertEquals(8, files.size());
                for (Object fileName : files) {
                    File file = null;
                    try {
                        file = new File(fileName.toString());
                        if (!file.isDirectory()) {
                            Scanner scanner = null;
                            scanner = new Scanner(file);
                            String line = scanner.nextLine();
                            context.assertTrue(line.endsWith(" you"));
                            scanner.close();
                        }
                    } catch (Exception e) {
                        context.fail(e);
                    }
                }
                async.complete();
            }
        });
    }

    @Test
    public void testWrongZip(TestContext context) {
        final Async async = context.async();
        vertx.eventBus().<JsonArray>send(ADDRESS, UPL_DIR + "/wrong.zip", replyHandler -> {
            if(replyHandler.failed()) {
            } else {
                context.fail("The Zip Worker should have failed for this file");
            }
            async.complete();
        });
    }

    @Test
    public void testUnexistingZip(TestContext context) {
        final Async async = context.async();
        vertx.eventBus().<JsonArray>send(ADDRESS, UPL_DIR + "/unexisting.zip", replyHandler -> {
            if(replyHandler.failed()) {
            } else {
                context.fail("The Zip Worker should have failed for this file");
            }
            async.complete();
        });
    }


}
