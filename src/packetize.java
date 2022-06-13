import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.CRC32;

import static java.lang.String.format;

public class packetize
{
    int CRC32_of_whole = 0;
    int CRC32_of_packet = 0;
    long size_of_packet = 0;
    long size_of_whole = 0;
    long last_packet_length = 0;
    long current_packet_position = 0;
    long total_packets_less_last = 0;
    byte[] full_array = null;
    byte[] current_packet_array = null;
    File current_file;
    boolean xmit_or_rcve = false;       // true == transmit false == receive
    int retry = 10;
    int current_retry = 0;
    serial_gui comm_port;

    public packetize(File current_file, long size_of_packet, boolean xmit_or_rcve, serial_gui comm_port) throws IOException {
        this.current_file = current_file;
        this.xmit_or_rcve = xmit_or_rcve;
        this.size_of_packet = size_of_packet;
        this.comm_port = comm_port;
        if(initialize_tx() == true)
            {
            int next_index = 0;
            int i = 0; int p = 0; int q = 0;
            for(i = 0; i<total_packets_less_last; i++)
                {
                    byte[] arr = getSliceOfArray(full_array, next_index, (int)(size_of_packet) + next_index );
                    System.out.print("Packet " + i + " = ");
                    for(p = 0; p < arr.length; p++)
                        {
                        System.out.print("0x" + String.format("%02X", arr[p]) + " ");
                        }
                    int[] arr_values = new int[(int)size_of_packet];
                    for(int pp = 0; pp<size_of_packet; pp++)
                    {
                        arr_values[pp] = arr[pp] & 0x0F;
                    }
                    CRC32_of_packet = java_crc32(arr);
                    String temp_crcp = convert_int_to_bytes(CRC32_of_packet).toString();
                        System.out.print(" 0x" + temp_crcp );
                        System.out.println(" ");
                next_index += size_of_packet;
                }
                byte[] last_arr = getSliceOfArray(full_array, next_index, (int)(last_packet_length) + next_index );
                System.out.print("Packet " + i + " = ");
                for(q = 0; q < last_arr.length; q++)
                    {
                    System.out.print("0x" + String.format("%02X", last_arr[q]) + " ");
                    }
                int[] last_arr_values = new int[(int)last_packet_length];
                for(int pq = 0; pq < last_packet_length; pq++)
                {
                    last_arr_values[pq] = last_arr[pq] & 0xFF;
                }
                CRC32_of_packet = java_crc32(last_arr);
                String temp_crce = convert_int_to_bytes(CRC32_of_packet).toString();
                System.out.print(" 0x" + temp_crce);
                System.out.println(" ");
                System.out.println("Passed packetization");
                if(send_tx_preamble() == true)
                {
                    if (send_packets() == true)
                        System.out.println("File Sent sucessfully");
                    else
                        System.out.println("File transfer unsuccessful");
                }
                    else
                        System.out.println("File preamble not successful, possibly no comm port open");
            }
        else
        {
        System.out.println("Failed to packetize");
        }
    }

    private boolean send_tx_preamble()
    {
        return true;
    }

    private boolean send_packets()
    {
        int i = 0;
        int next_index = 0;
        try {
            for(i = 0; i < total_packets_less_last; i++)
            {
                byte[] arr = getSliceOfArray(full_array, next_index, (int)(size_of_packet) + next_index );
                int[] arr_values = new int[(int)size_of_packet];
                for(int p = 0; p < size_of_packet; p++)
                    {
                    arr_values[p] = arr[p] & 0xFF;
                    }
                CRC32_of_packet = java_crc32(arr);
                comm_port.specific_process[0].send_simple_byte(arr, 0, false);
                String temp_crcp= convert_int_to_bytes(CRC32_of_packet).toString();
                comm_port.specific_process[0].send_simple_binary(temp_crcp, 1, false);
                next_index += size_of_packet;
            }
            byte[] last_arr = getSliceOfArray(full_array, next_index, (int)(last_packet_length) + next_index );
            int[] last_arr_values = new int[(int)last_packet_length];
            for(int pp = 0; pp < last_packet_length; pp++)
                {
                    last_arr_values[pp] = last_arr[pp] & 0xFF;
                }
            CRC32_of_packet = java_crc32(last_arr);
            comm_port.specific_process[0].send_simple_byte(last_arr, 0, false);
            String temp_crce = convert_int_to_bytes(CRC32_of_packet).toString();
            comm_port.specific_process[0].send_simple_binary(temp_crce, 1, false);
        }
        catch (Exception e)
        {
            System.out.println(e);
            return false;
        }
        return true;
    }

    private StringBuilder convert_int_to_bytes(int val)
    {
    byte[] bytes = ByteBuffer.allocate(4).putInt(val).array();;
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes)
        {
            sb.append(String.format("%02X", b));
        }
    return sb;
    }

    private int java_crc32(byte[] packet_array) throws IOException
    {
        int crc;
        CRC32 my_crc32 = new CRC32();
        my_crc32.update(packet_array);
        crc = (int)my_crc32.getValue();
        //String temp_str = String.format("%08X", crc);
        //System.out.println("Test Alternate java_crc32() " + Integer.toHexString(crc));
        //System.out.println("Test Alternate java_crc32() " + temp_str);
        return crc & 0xFFFFFFFF;
    }

    private boolean initialize_tx()
    {
        try {
            full_array = Files.readAllBytes(Paths.get(current_file.getAbsolutePath())) ;
            size_of_whole =  full_array.length;
            System.out.println("Size of File " + size_of_whole + " bytes");
            CRC32_of_whole = java_crc32(full_array);
            System.out.println("CRC32 of File 0x" + Integer.toHexString(CRC32_of_whole));
            current_packet_array = getSliceOfArray(full_array, 0, (int)(size_of_packet));
            CRC32_of_packet = java_crc32(current_packet_array);
            System.out.println("CRC32 of first Packet 0x" + Integer.toHexString(CRC32_of_packet) + " of " + size_of_packet + " bytes");
            current_retry = 0;
            current_packet_position = 0;
            total_packets_less_last = size_of_whole / size_of_packet;
            System.out.println("Number of " + size_of_packet + " byte packets, less last is " + total_packets_less_last);
            last_packet_length = size_of_whole % size_of_packet;
            System.out.println("Length of last packet " + last_packet_length + " bytes");
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private boolean initialize_rx()
    {
        try {
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static byte[] getSliceOfArray(byte[] arr, int startIndex, int endIndex)
    {
        // Get the slice of the Array
        byte[] slice = Arrays.copyOfRange(arr, startIndex, endIndex);
        return slice;
    }

    public int getCRC32_of_whole() {
        return CRC32_of_whole;
    }

    public void setCRC32_of_whole(int CRC32_of_whole) {
        this.CRC32_of_whole = CRC32_of_whole;
    }

    public int getCRC32_of_packet() {
        return CRC32_of_packet;
    }

    public void setCRC32_of_packet(int CRC32_of_packet) {
        this.CRC32_of_packet = CRC32_of_packet;
    }

    public long getSize_of_packet() {
        return size_of_packet;
    }

    public void setSize_of_packet(long size_of_packet) {
        this.size_of_packet = size_of_packet;
    }

    public long getSize_of_whole() {
        return size_of_whole;
    }

    public void setSize_of_whole(long size_of_whole) {
        this.size_of_whole = size_of_whole;
    }

    public long getCurrent_packet_position() {
        return current_packet_position;
    }

    public void setCurrent_packet_position(long current_packet_position) {
        this.current_packet_position = current_packet_position;
    }

    public byte[] getFull_array() {
        return full_array;
    }

    public void setFull_array(byte[] full_array) {
        this.full_array = full_array;
    }

    public byte[] getCurrent_packet_array() {
        return current_packet_array;
    }

    public void setCurrent_packet_array(byte[] current_packet_array) {
        this.current_packet_array = current_packet_array;
    }

    public File getCurrent_file() {
        return current_file;
    }

    public void setCurrent_file(File current_file) {
        this.current_file = current_file;
    }

    public boolean isXmit_or_rcve() {
        return xmit_or_rcve;
    }

    public void setXmit_or_rcve(boolean xmit_or_rcve) {
        this.xmit_or_rcve = xmit_or_rcve;
    }

}
