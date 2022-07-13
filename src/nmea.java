import processing.core.PApplet;

public class nmea extends PApplet
{
static
{
    System.loadLibrary("NMEA0183");
}
public nmea()
{
    println(System.getProperty("java.library.path"));
    String new_sentence = "$GPGGA,104435.12,3337.19,N,11158.43,W,1,06,4.5,,,,,,\r\n";

    //NMEA0183 nmea0183;
    //nmea0183.SetSentence(new_sentence);
    //nmea0183.Parse();
    //println("%s\r\n", nmea0183.LastSentenceIDParsed.c_str());
    //println("%s\r\n", nmea0183.LastSentenceIDReceived.c_str());
    //println("%s\r\n", nmea0183.PlainText.c_str());
    //println("Number of Satellites %d", nmea0183.Gga.NumberOfSatellitesInUse);
}
    public native void initCPP_NMEA0183();
    public native void SetSentence(String source);
    public native boolean Parse();
}
