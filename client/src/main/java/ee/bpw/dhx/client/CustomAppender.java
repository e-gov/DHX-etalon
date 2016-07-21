package ee.bpw.dhx.client;



import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Custom LOG4J appender. Needed to contain and log EVENTs occured in DHX application.
 * 
 * @author Aleksei Kokarev
 *
 */

@Plugin(name = "CustomAppender", category = "Core", elementType = "appender", printObject = true)
public final class CustomAppender extends AbstractAppender {

  private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
  private final Lock readLock = rwLock.readLock();

  private static Queue<String> queue = new LinkedList<String>();
  private int maxQueueSize = 20;

  protected CustomAppender(String name, Filter filter, Layout<? extends Serializable> layout,
      final boolean ignoreExceptions) {
    super(name, filter, layout, ignoreExceptions);
  }

  // The append method is where the appender does the work.
  // Given a log event, you are free to do with it what you want.
  // This example demonstrates:
  // 1. Concurrency: this method may be called by multiple threads concurrently
  // 2. How to use layouts
  // 3. Error handling
  @Override
  public void append(LogEvent event) {
    readLock.lock();
    try {
      if (queue.size() >= maxQueueSize) {
        queue.remove();
        queue.add(new String(getLayout().toByteArray(event)));
      } else {
        queue.add(new String(getLayout().toByteArray(event)));
      }
    } catch (Exception ex) {
      if (!ignoreExceptions()) {
        throw new AppenderLoggingException(ex);
      }
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Pluginfactory.
   * 
   * @param name - name
   * @param layout - layout
   * @param filter - filter
   * @param otherAttribute - other attribute
   * @return - customAppender
   */
  @PluginFactory
  public static CustomAppender createAppender(@PluginAttribute("name") String name,
      @PluginElement("Layout") Layout<? extends Serializable> layout,
      @PluginElement("Filter") final Filter filter,
      @PluginAttribute("otherAttribute") String otherAttribute) {
    if (name == null) {
      LOGGER.error("No name provided for MyCustomAppenderImpl");
      return null;
    }
    if (layout == null) {
      layout = PatternLayout.createDefaultLayout();
    }
    return new CustomAppender(name, filter, layout, true);
  }

  /**
   * Returns last logged events.
   * 
   * @return - last logged events
   */
  public static String getLastEvents() {
    String lastLog = "";
    for (String event : queue) {
      lastLog = event + lastLog;
    }
    return lastLog;
  }

  /**
   * deletes log events.
   */
  public static void deleteLastEvents() {
    queue = new LinkedList<String>();
  }
}
