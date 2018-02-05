using System;
using System.Net;
using System.Net.Sockets;
using vJoyInterfaceWrap;

namespace SFC_Satellite
{
	class Satellite
	{
		public static void Fingerprint(float x, float y, int w, int h){
			int xx = (int)Math.Round(((x + 1f) / 2f) * (float)w), yy = (int)Math.Round(((y + 1f) / 2f) * (float)h);
			for (int yyy = 0; yyy < h; ++yyy){
				for (int xxx = 0; xxx < w; ++xxx)
					if (xxx == xx || yyy == yy)
						Console.Write("[X]");
					else
						Console.Write("[ ]");
				Console.WriteLine();
			}
		}
		
		public static float Remap (float value, float from1, float to1, float from2, float to2) {
			return (value - from1) / (to1 - from1) * (to2 - from2) + from2;
		}
		
		public static void Main(string[] args)
		{
			UdpClient antennae = new UdpClient(10777);
			IPEndPoint ep = new IPEndPoint(IPAddress.Any, 10777);  
			vJoy device = new vJoy();
			device.ResetVJD(1);
			long max = 0, min = 0, slmin = 0, slmax = 0;
			device.GetVJDAxisMax(1, HID_USAGES.HID_USAGE_X, ref max);
			device.GetVJDAxisMin(1, HID_USAGES.HID_USAGE_X, ref min);
			device.GetVJDAxisMax(1, HID_USAGES.HID_USAGE_SL0, ref slmax);
			device.GetVJDAxisMin(1, HID_USAGES.HID_USAGE_SL0, ref slmin);

			VjdStat status = device.GetVJDStatus(1);
			if ((status == VjdStat.VJD_STAT_OWN) || ((status == VjdStat.VJD_STAT_FREE) && (!device.AcquireVJD(1)))){
				Console.WriteLine("Launch failed");
				Console.ReadKey(true);
				return ;
			} else
				Console.WriteLine("Sattelite launched");
			
			while (true){
				byte[] buff = antennae.Receive(ref ep);
				float x, y, hatx, haty, thr;
				
				if (BitConverter.IsLittleEndian){
					Array.Reverse(buff, 0, 4);
					Array.Reverse(buff, 4, 4);
					Array.Reverse(buff, 8, 4);
					Array.Reverse(buff, 12, 4);
					Array.Reverse(buff, 16, 4);
				}
				
				x = BitConverter.ToSingle(buff, 0);
				y = BitConverter.ToSingle(buff, 4);
				hatx = BitConverter.ToSingle(buff, 8);
				haty = BitConverter.ToSingle(buff, 12);
				thr = BitConverter.ToSingle(buff, 16);
				
				device.SetAxis((int)Remap(x, -1, 1, min, max), 1, HID_USAGES.HID_USAGE_X);
				device.SetAxis((int)Remap(y, -1, 1, min, max), 1, HID_USAGES.HID_USAGE_Y);
				device.SetAxis((int)Remap(hatx, -1, 1, min, max), 1, HID_USAGES.HID_USAGE_RX);
				device.SetAxis((int)Remap(haty, -1, 1, min, max), 1, HID_USAGES.HID_USAGE_RY);
				device.SetAxis((int)Remap(thr, -1, 1, slmin, slmax), 1, HID_USAGES.HID_USAGE_SL0);
			}
		}
	}
}