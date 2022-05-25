import at.mukprojects.console.Console;
import g4p_controls.GButton;
import g4p_controls.GEvent;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import processing.core.PApplet;
import processing.core.PSurface;
import processing.serial.Serial;

import java.awt.*;
import java.util.Locale;

public class launch extends PApplet
{
serial_gui new_serial;
GButton btn_exit;
GButton btn_open;
GButton btn_send;
GButton btn_file_open;

public static int x_location = 0;
public static int y_location = 0;

public String Serial_Config_Version = "0_1_4";
public Console console;
public static int associated_process = 0;
public file_class data_dump;

public void settings()
{
  size(1000, 400);
}

public void setup()
{
  println("Serial Processing runtime configuration V" + Serial_Config_Version);
  noLoop();
  surface.setLocation((displayWidth/2) - 100, (displayHeight/2) - (height/2));  
    x_location = getJFrame(getSurface()).getX();
    y_location = getJFrame(getSurface()).getY();  
    
  configure_logger();
  change_logger_output(LOGGER.FILE_LOGGER);    // LOGGER.FILE_LOGGER if you want to move debug logging to a file.
                                               // File will be here ./data/log.txt
  version_info();
  
  btn_exit = new GButton(this, width-70, height - 40, 65, 30);
  btn_exit.setText("Exit");
  btn_exit.addEventHandler(this, "btn_exit_click"); 
  
  btn_open = new GButton(this, 10, height - 40, 65, 30);
  btn_open.setText("Open");
  btn_open.addEventHandler(this, "btn_open_click"); 
  
  btn_send = new GButton(this, 85, height - 40, 65, 30);
  btn_send.setText("Send");
  btn_send.addEventHandler(this, "btn_send_click");
  
  btn_file_open = new GButton(this, 160, height - 40, 65, 30);
  btn_file_open.setText("File Open");
  btn_file_open.addEventHandler(this, "btn_file_open_click"); 
  console = new Console(this);
  console.start();
  loop();
}

public void draw()
{
  // console.draw(x, y, width, height, preferredTextSize, minTextSize, linespace, padding, strokeColor, backgroundColor, textColor)
  console.draw(10, height - height +10, width-20, height - 60, 15, 15, 3, 3, color(220), color(0), color(240));
  console.print();
}

public void btn_exit_click(GButton source, GEvent event)
  { //_CODE_:btn_exit_click:443832:
  if(source == btn_exit && event == GEvent.CLICKED)
    {  
    System.exit(0);
    }
  } //_CODE_:btn_exit_click:443832:
  
public void btn_open_click(GButton source, GEvent event)
  { //_CODE_:btn_open_click:443832:
  if(source == btn_open && event == GEvent.CLICKED)
    {   
    new_serial = new serial_gui(this, x_location, y_location);   
    }
  } //_CODE_:btn_open_click:443832: 
  

public void btn_file_open_click(GButton source, GEvent event) 
  { //_CODE_:btn_file_open_click:443832:
  if(source == btn_file_open && event == GEvent.CLICKED)
    {
    try
      {
       data_dump = open_for_write_to_file(this, associated_process);
      }
    catch (Exception e)
      {
        Logger.info("Comm port not yet open :- " + this);
        println("Comm port not yet open");
      }
    }
  } //_CODE_:btn_file_open_click:443832: 
  
// Example to send data out the serial port using a button.
// Note Comm Port needs to be open for following function to work

public void btn_send_click(GButton source, GEvent event) throws Exception
  { //_CODE_:btn_send_click:443832:
  if(source == btn_send && event == GEvent.CLICKED)
    {
      try
      {
      String send_string = "Testing TX Data " + new_serial.specific_process[associated_process].getMyPortname();
      new_serial.specific_process[associated_process].send_serial_command(send_string, 100, false);
      }  
    catch(Exception e)
      {
      Logger.info("Comm port not yet open :- " + this);
      println("Comm port not yet open");
      }
    }
  } //_CODE_:btn_send_click:443832:

public void serialEvent(Serial p)
{
  if(operate_on_new_serial_event(p) == true)
    {
    String inString = return_rx_serial_data(p);
    Logger.info("Serial Event " + return_serial_port_name(p) + " :- " + inString +  " Process :- " + p);
    if(data_dump != null)
      {
        data_dump.file_append(inString + "\n");
        println(return_serial_port_name(p) + " :- " + inString);
      }
    else
        println("Serial Event " + return_serial_port_name(p) + " :- " + inString +  " Process :- " + p);
    }
}

@SuppressWarnings("unused")
public void handleButtonEvents(GButton button, GEvent event) 
{ /* code */ 

}

  enum LOGGER
  {
    NO_LOGGER,
    FILE_LOGGER,
    CONSOLE_LOGGER
  }

  public void change_logger_output(LOGGER log)
  {
    if(log == LOGGER.FILE_LOGGER)
    {
      Configurator.currentConfig()
              .writer(new org.pmw.tinylog.writers.FileWriter(sketchPath("/data/log.txt")))
              .activate();
    }
    if(log == LOGGER.CONSOLE_LOGGER)
    {
      Configurator.currentConfig()
              .writer(new ConsoleWriter())
              .activate();
    }
  }

  public void change_logger_level(Level level)
  {
    Configurator.currentConfig()
            .level(level)
            .activate();
  }

  public void configure_logger()
  {
    Configurator.currentConfig()
            .writer(new org.pmw.tinylog.writers.FileWriter(sketchPath("/data/log.txt")))
            .formatPattern("{date:yyyy-MM-dd HH:mm:ss} {{level}|min-size=5} {class_name}.{method} - {message}")
            .level(Level.INFO)
            .locale(Locale.US)
            .activate();
  }

  public int which_port_generated_serial_event(Serial p)
  {
    int port = 0;
    if(p == new_serial.specific_process[0].getMyPort())
      port = 0;
    if(p == new_serial.specific_process[1].getMyPort())
      port = 1;
    if(p == new_serial.specific_process[2].getMyPort())
      port = 2;
    if(p == new_serial.specific_process[3].getMyPort())
      port = 3;
    return port;
  }

  public String return_rx_serial_data(Serial p)
  {
    new_serial.specific_process[which_port_generated_serial_event(p)].serialEvent(p);
    String inString = new_serial.specific_process[which_port_generated_serial_event(p)].strarray_rx[new_serial.specific_process[which_port_generated_serial_event(p)].current_rx_pointer];
    Logger.debug("Current rx_pointer :- " + new_serial.specific_process[which_port_generated_serial_event(p)].current_rx_pointer);
    new_serial.specific_process[which_port_generated_serial_event(p)].current_rx_pointer ++;
    new_serial.specific_process[which_port_generated_serial_event(p)].current_rx_pointer %= 4;
    return inString;
  }

  public String return_serial_port_name(Serial p)
  {
    return new_serial.specific_process[which_port_generated_serial_event(p)].getMyPortname();
  }

  public boolean operate_on_new_serial_event(Serial p)
  {
    boolean is_valid = false;
    try
    {
      new_serial.specific_process[which_port_generated_serial_event(p)].serialEvent(p);
      is_valid = true;
    }
    catch (Exception e)
    {
      Logger.info("Not a valid Serial Event :- " + this);
      is_valid = false;
    }
    return is_valid;
  }

  public file_class open_for_write_to_file(PApplet p, int process)
  {
    String my_path = p.sketchPath();
    file_class new_file = new file_class(my_path + "\\data\\" + new_serial.specific_process[process].getsave_file() + ".dat");
    Logger.info(new_file.get_new_file());
    return new_file;
  }

  static final javax.swing.JFrame getJFrame(final PSurface surface)
  {
    return (javax.swing.JFrame) ( (processing.awt.PSurfaceAWT.SmoothCanvas) surface.getNative()).getFrame();
  }
  public void version_info()
  {
    Logger.info("************************************");
    Logger.info("Application Launched");
    Logger.info("Serial Configuration Version = " + Serial_Config_Version);
  }

  /**
   * Main entry point
   * @param _args command line arguments
   */
  public static void main(String[] _args)
  {
    PApplet.main(new String[]
            {
                    launch.class.getName()
            }
    );
  }

}
