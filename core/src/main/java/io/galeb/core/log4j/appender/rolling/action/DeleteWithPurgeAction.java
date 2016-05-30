/**
 *
 */
package io.galeb.core.log4j.appender.rolling.action;

import org.apache.logging.log4j.core.appender.rolling.action.AbstractPathAction;
import org.apache.logging.log4j.core.appender.rolling.action.DeletingVisitor;
import org.apache.logging.log4j.core.appender.rolling.action.PathCondition;
import org.apache.logging.log4j.core.appender.rolling.action.PathSortByModificationTime;
import org.apache.logging.log4j.core.appender.rolling.action.PathSorter;
import org.apache.logging.log4j.core.appender.rolling.action.PathWithAttributes;
import org.apache.logging.log4j.core.appender.rolling.action.ScriptCondition;
import org.apache.logging.log4j.core.appender.rolling.action.SortingVisitor;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Example:
 *
 * <DefaultRolloverStrategy max="5">
 *   <DeleteWithPurge basePath="${baseDir}" purgeTo="/tmp/purged/">
 *     <IfFileName glob="app-*.log.gz" />
 *   </DeleteWithPurge>
 * </DefaultRolloverStrategy>
 */

/**
 * HINT: pom.xml
 * <plugin>
 *   <groupId>org.apache.maven.plugins</groupId>
 *   <artifactId>maven-shade-plugin</artifactId>
 *   <executions>
 *     <execution>
 *       <phase>package</phase>
 *       <goals>
 *         <goal>shade</goal>
 *       </goals>
 *       <configuration>
 *         ...
 *         <transformers>
 *           <transformer
 *              implementation="com.github.edwgiz.mavenShadePlugin.log4j2CacheTransformer.PluginsCacheFileTransformer">
 *           </transformer>
 *         </transformers>
 *       </configuration>
 *     </execution>
 *   </executions>
 *   <dependencies>
 *     <dependency>
 *       <groupId>com.github.edwgiz</groupId>
 *       <artifactId>maven-shade-plugin.log4j2-cachefile-transformer</artifactId>
 *       <version>2.1</version>
 *     </dependency>
 *   </dependencies>
 * </plugin>
 */

@Plugin(name = "DeleteWithPurge", category = "Core", printObject = true)
public class DeleteWithPurgeAction extends AbstractPathAction {

    private final boolean testMode;
    private final PathSorter pathSorter;
    private final ScriptCondition scriptCondition;
    private final String purgeTo;

    private DeleteWithPurgeAction(final String basePath, final String purgeTo, final boolean followSymbolicLinks, final int maxDepth,
                                  final boolean testMode, final PathSorter sorter, final PathCondition[] conditions,
                                  final ScriptCondition scriptCondition, final StrSubstitutor subst) {
        super(basePath, followSymbolicLinks, maxDepth, conditions, subst);
        this.purgeTo = purgeTo;
        this.testMode = testMode;
        this.pathSorter = Objects.requireNonNull(sorter, "sorter");
        this.scriptCondition = scriptCondition;
        if (scriptCondition == null && (conditions == null || conditions.length == 0)) {
            LOGGER.error("Missing Delete conditions: unconditional Delete not supported");
            throw new IllegalArgumentException("Unconditional Delete not supported");
        }
    }

    @Override
    public boolean execute() throws IOException {
        return scriptCondition != null ? executeScript() : super.execute();
    }

    private boolean executeScript() throws IOException {
        final List<PathWithAttributes> selectedForDeletion = callScript();
        if (selectedForDeletion == null) {
            LOGGER.trace("Script returned null list (no files to purge)");
            return true;
        }
        deleteSelectedFiles(selectedForDeletion);
        return true;
    }

    private List<PathWithAttributes> callScript() throws IOException {
        final List<PathWithAttributes> sortedPaths = getSortedPaths();
        trace("Sorted paths:", sortedPaths);
        return scriptCondition.selectFilesToDelete(getBasePath(), sortedPaths);
    }

    private void deleteSelectedFiles(final List<PathWithAttributes> selectedForDeletion) throws IOException {
        trace("Paths the script selected for deletion:", selectedForDeletion);
        for (final PathWithAttributes pathWithAttributes : selectedForDeletion) {
            final Path path = pathWithAttributes == null ? null : pathWithAttributes.getPath();
            delete(path);
        }
    }

    private void delete(final Path path) throws IOException {
        if (isTestMode()) {
            LOGGER.info("Purge {} to {} (TEST MODE: file not actually purged)", path, purgeTo);
        } else {
            purge(path);
        }
    }

    private void purge(final Path path) throws IOException {
        if (purgeTo != null) {
            String localFullPurgeTo = purgeTo + "/" + path.toString();
            if (!purgeTo.startsWith("/")) {
                localFullPurgeTo = System.getenv("PWD") + "/" + localFullPurgeTo;
            }
            Path fullPurgeToPath = Paths.get(localFullPurgeTo);
            Path purgeToPath = Paths.get(purgeTo);
            boolean isDir = Files.isDirectory(purgeToPath);
            boolean isDirWritable = Files.isWritable(purgeToPath);
            if (!Files.exists(path)) {
                LOGGER.warn("File "+path+" not exists");
                return;
            }
            if (!Files.exists(purgeToPath)) {
                LOGGER.error("Target "+purgeToPath+" not exists. Aborting purge.");
                return;
            }
            if (!isDir) {
                LOGGER.error(purgeToPath+" is not directory. Aborting purge.");
                return;
            }
            if (!isDirWritable) {
                LOGGER.error("Directory "+purgeToPath+" is not writable. Aborting purge.");
                return;
            }
            LOGGER.trace("Moving "+path+" to "+purgeToPath);
            Files.move(path, fullPurgeToPath, REPLACE_EXISTING);
        } else {
            LOGGER.trace("Deleting "+path);
            Files.deleteIfExists(path);
        }
    }

    @Override
    public boolean execute(final FileVisitor<Path> visitor) throws IOException {
        final List<PathWithAttributes> sortedPaths = getSortedPaths();
        trace("Sorted paths:", sortedPaths);

        for (PathWithAttributes element : sortedPaths) {
            try {
                visitor.visitFile(element.getPath(), element.getAttributes());
            } catch (final IOException ioex) {
                LOGGER.error("Error in post-rollover Delete when visiting {}", element.getPath(), ioex);
                visitor.visitFileFailed(element.getPath(), ioex);
            }
        }
        return true; // do not abort rollover even if processing failed
    }

    private void trace(final String label, final List<PathWithAttributes> sortedPaths) {
        LOGGER.trace(label);
        for (final PathWithAttributes pathWithAttributes : sortedPaths) {
            LOGGER.trace(pathWithAttributes);
        }
    }

    private List<PathWithAttributes> getSortedPaths() throws IOException {
        final SortingVisitor sort = new SortingVisitor(pathSorter);
        super.execute(sort);
        return sort.getSortedPaths();
    }

    private boolean isTestMode() {
        return testMode;
    }

    @Override
    protected FileVisitor<Path> createFileVisitor(Path visitorBaseDir, List<PathCondition> conditions) {
        return new DeleteWithPurgeVisitor(visitorBaseDir, conditions, testMode, this);
    }

    @PluginFactory
    public static DeleteWithPurgeAction createDeleteCustomAction(
            @PluginAttribute("basePath") final String basePath, //
            @PluginAttribute("purgeTo") final String purgeTo, //
            @PluginAttribute(value = "followLinks", defaultBoolean = false) final boolean followLinks,
            @PluginAttribute(value = "maxDepth", defaultInt = 1) final int maxDepth,
            @PluginAttribute(value = "testMode", defaultBoolean = false) final boolean testMode,
            @PluginElement("PathSorter") final PathSorter sorterParameter,
            @PluginElement("PathConditions") final PathCondition[] pathConditions,
            @PluginElement("ScriptCondition") final ScriptCondition scriptCondition,
            @PluginConfiguration final Configuration config) {

        final PathSorter sorter = sorterParameter == null ? new PathSortByModificationTime(true) : sorterParameter;

        return new DeleteWithPurgeAction(basePath, purgeTo, followLinks, maxDepth, testMode, sorter, pathConditions,
                                        scriptCondition, config.getStrSubstitutor());
    }

    private static class DeleteWithPurgeVisitor extends DeletingVisitor {

        private final DeleteWithPurgeAction deleteWithPurgeAction;

        DeleteWithPurgeVisitor(final Path basePath, final List<? extends PathCondition> pathConditions,
                               boolean testMode, final DeleteWithPurgeAction action) {
            super(basePath, pathConditions, testMode);
            this.deleteWithPurgeAction = action;
        }

        @Override
        protected void delete(Path file) throws IOException {
            deleteWithPurgeAction.delete(file);
        }
    }
}
