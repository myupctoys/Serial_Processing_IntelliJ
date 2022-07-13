import at.mukprojects.console.Console;
import g4p_controls.*;
import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Level;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.writers.ConsoleWriter;
import processing.core.PApplet;
import processing.core.PSurface;
import processing.serial.Serial;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class launch extends PApplet
{
serial_gui new_serial;
GButton btn_exit;
GButton btn_open;
GButton btn_send;
GButton btn_file_open;
GButton btn_send_file;
GTextField txt_string_to_send;
GPanel pnl_launch;
GOption opt_date_time_stamp;

public static int x_location = 0;
public static int y_location = 0;

public String Serial_Config_Version = "0_1_7";
public Console console;
public static int associated_process = 0;
public static file_class[] data_dump;

xfer new_xfer;

public void settings()
{
  size(1000, 490);
}

public void setup()
{
  GOL gol = new GOL();
  println("Serial Processing runtime configuration V" + Serial_Config_Version);
  noLoop();
  surface.setLocation((displayWidth/2) - 100, (displayHeight/2) - (height/2));
  x_location = getJFrame(getSurface()).getX();
  y_location = getJFrame(getSurface()).getY();
    
  configure_logger();
  change_logger_output(LOGGER.FILE_LOGGER);    // LOGGER.FILE_LOGGER if you want to move debug logging to a file.
                                               // File will be here ./data/log.txt
  version_info();

  pnl_launch = new GPanel(this, 10, height -70, width - 20, 60, "");
  btn_exit = new GButton(this, width-90, 25, 65, 30);
  btn_exit.setText("Exit");
  btn_exit.addEventHandler(this, "btn_exit_click");
  pnl_launch.addControl(btn_exit);
  
  btn_open = new GButton(this, 10, 25, 65, 30);
  btn_open.setText("Open");
  btn_open.addEventHandler(this, "btn_open_click");
  pnl_launch.addControl(btn_open);

  btn_send = new GButton(this, 85, 25, 65, 30);
  btn_send.setText("Send");
  btn_send.addEventHandler(this, "btn_send_click");
  pnl_launch.addControl(btn_send);

  txt_string_to_send = new GTextField(this, 160, 30, 120, 20);
  txt_string_to_send.setText("Testing TX Data");
  pnl_launch.addControl(txt_string_to_send);

  btn_file_open = new GButton(this, 290, 25, 65, 30);
  btn_file_open.setText("File Open");
  btn_file_open.addEventHandler(this, "btn_file_open_click");
  pnl_launch.addControl(btn_file_open);

  opt_date_time_stamp = new GOption(this, 365, 25, 100, 30, "DTG Stamp");
  pnl_launch.addControl(opt_date_time_stamp);

  btn_send_file = new GButton(this,470, 25, 65, 30);
  btn_send_file.setText("Send File");
  btn_send_file.addEventHandler(this, "btn_send_file_click");
  pnl_launch.addControl(btn_send_file);

  data_dump = new file_class[4];

  console = new Console(this);
  console.start();
  loop();
}

public void draw()
{
  // console.draw(x, y, width, height, preferredTextSize, minTextSize, linespace, padding, strokeColor, backgroundColor, textColor)
  console.draw(10, height - height + 10, width-20, height - 90, 15, 15, 4, 4, color(220), color(0), color(240));
  console.print();
}

public String generate_dtg()
{
  return day() + ":" + month() + ":" + year() + " " + hour() + ":" + minute() + ":" + second();
}

public String generate_dtg_file()
  {
    return day() + "_" + month() + "_" + year() + " " + hour() + "_" + minute() + "_" + second();
  }

public void btn_send_file_click(GButton source, GEvent event) throws IOException
  {
    thread("send_file_thread");
    //new_xfer = new xfer(file, null, new_serial.specific_process[associated_process].getMyPort());
  }

public void send_file_thread() throws IOException
  {
    try
    {
      File file = new File("E:\\JSandbox\\Serial_Processing_IntelliJ\\lib\\base\\data\\test.png");
      new_xfer = new xfer(file, null, new_serial.specific_process[associated_process].getMyPort());
    }
    catch(Exception e)
    {
      println("Either file doesn't exist, or serial port not opened");
    }
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
      Logger.info("btn_file_open_click clicked");
    try
      {
        for(int i = 0; i < 4; i++)
        {
          if (new_serial.specific_process[i].getPort_is_open() == true)
            {
            data_dump[i] = open_for_write_to_file(this, i);
            }
          else
          {
            Logger.info("Comm port not yet open :- " + this);
            println("Comm port not yet open");
          }
        }
      }
    catch (Exception e)
      {
        Logger.info("Comm port not yet open or file already open :- " + this);
        println("Comm port not yet open or file already open");
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
      String send_string = txt_string_to_send.getText() + " " + new_serial.specific_process[associated_process].getMyPortname();
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
    String writeinString = "";
    Logger.info("Serial Event " + return_serial_port_name(p) + " :- " + inString +  " Process :- " + p);
    int port_that_caused_event = which_port_generated_serial_event(p);
    if(data_dump[port_that_caused_event] != null)
      {
        if(opt_date_time_stamp.isSelected() == true)
          {
          writeinString = generate_dtg() + "," + inString;
          }
        else
        {
          writeinString = inString;
        }
        data_dump[port_that_caused_event].file_append(writeinString + "\n");
        println(return_serial_port_name(p) + " :- " + writeinString);
      }
    else
    {
      writeinString = inString;
      println("Serial Event " + return_serial_port_name(p) + " :- " + writeinString + " Process :- " + p);
    }

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
    file_class new_file = new file_class(my_path + "\\data\\" + new_serial.specific_process[process].getsave_file() + " " + generate_dtg_file() + ".csv");
    Logger.info(new_file.get_new_file());
    return new_file;
  }

  public void handlePanelEvents(GPanel panel, GEvent event) { /* code */ }
  public void handleToggleControlEvents(GToggleControl option, GEvent event) { /* code */ }
  public void handleTextEvents(GEditableTextControl textcontrol, GEvent event) { /* code */ }

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
