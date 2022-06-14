import processing.serial.Serial;

import java.io.File;
import java.io.IOException;

public class xfer
{

    File file_name_to_rcve;
    packetize rx_packet;
    File file_name_to_xmit;
    packetize tx_packet;
    Serial comm_port;

    Boolean xmit_or_rcve = false;

    public File getFile_name_to_xmit() {
        return file_name_to_xmit;
    }

    public void setFile_name_to_xmit(File file_name_to_xmit) {
        this.file_name_to_xmit = file_name_to_xmit;
    }

    public File getFile_name_to_rcve() {
        return file_name_to_rcve;
    }

    public void setFile_name_to_rcve(File file_name_to_rcve) {
        this.file_name_to_rcve = file_name_to_rcve;
    }

    public xfer()
    {
    }

    public xfer(File file_name_to_xmit, File file_name_to_rcve, Serial comm_port) throws IOException
    {
        this.comm_port = comm_port;

        if(file_name_to_xmit != null)
        {
            this.file_name_to_xmit = file_name_to_xmit;
            xmit_or_rcve = true;
            File new_file = file_name_to_xmit;
            tx_packet = new packetize(new_file, 64, xmit_or_rcve, comm_port);
        }
        if(file_name_to_rcve != null) {
            this.file_name_to_rcve = file_name_to_rcve;
            xmit_or_rcve = false;
            File new_file = file_name_to_rcve;
            rx_packet = new packetize(new_file, 64, xmit_or_rcve, comm_port);
        }

    }
}
