/************************************************************************************************
 *   ____________ _   _  _____          _      _____ _    _ _______          __   _ _      _   
 *  |___  /  ____| \ | |/ ____|        | |    / ____| |  | |_   _\ \        / /  | | |    | |  
 *     / /| |__  |  \| | |     __ _ ___| |__ | |  __| |  | | | |  \ \  /\  / /_ _| | | ___| |_ 
 *    / / |  __| | . ` | |    / _` / __| '_ \| | |_ | |  | | | |   \ \/  \/ / _` | | |/ _ \ __|
 *   / /__| |____| |\  | |___| (_| \__ \ | | | |__| | |__| |_| |_   \  /\  / (_| | | |  __/ |_ 
 *  /_____|______|_| \_|\_____\__,_|___/_| |_|\_____|\____/|_____|   \/  \/ \__,_|_|_|\___|\__|
 *                                                                                             
 * Copyright (c) 2017 Ivan Vaklinov <ivan@vaklinov.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 **********************************************************************************/
package com.vaklinov.zcashui.msg;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.vaklinov.zcashui.CommandExecutor;
import com.vaklinov.zcashui.Log;
import com.vaklinov.zcashui.OSUtil;
import com.vaklinov.zcashui.OSUtil.OS_TYPE;
import com.vaklinov.zcashui.ZCashInstallationObserver;
import com.vaklinov.zcashui.ZCashInstallationObserver.DAEMON_STATUS;
import com.vaklinov.zcashui.ZCashInstallationObserver.DaemonInfo;


/**
 * Encapsulates access to IPFS for file sharing.
 *
 * @author Ivan Vaklinov <ivan@vaklinov.com>
 */
public class IPFSWrapper
{
	private JFrame parentFrame;
	
	private Process IPFSProcess;
	
	public IPFSWrapper(JFrame parentFrame)
	{
		this.parentFrame = parentFrame;
		this.IPFSProcess = null;
	}
	

	public String shareFileViaIPFS()
		throws IOException, InterruptedException
	{
		System.out.println("Sharing file ...");
		
		this.ensureIPFSIsRunning();
		
		// TODO: delay to start - check is we started it etc...
		
		String ipfs = this.getIPFSFullExecutablePath();
		
		CommandExecutor exec = new CommandExecutor(new String[]
		{
			ipfs, "add", "/home/ivan/temp/zer of ile.dat"	   
		});
		
		String strResponse = exec.execute();
		
		System.out.println("Response is: " + strResponse);
		
		return null;
	}
	
	
	
	private boolean ensureIPFSIsRunning()
		throws IOException, InterruptedException
	{
		if (!isIPFSWrapperRunning())
		{
			if (!this.getUserConsentToStartIPFS())
			{
				return false;
			}
			
			this.startIPFS();
			
			// TODO: delay here...
		}
		
		return true;
	}
	
	
	private void startIPFS()
		throws IOException
	{
		CommandExecutor starter = new CommandExecutor(
		    new String[] 
		    {
		        this.getIPFSFullExecutablePath(), "daemon"
		    });
		    
		this.IPFSProcess = starter.startChildProcess();
		
        Runtime.getRuntime().addShutdownHook(new Thread() 
        {
            public void run() 
            {
            	Log.info("Stopping IPFS...");
                try 
                {
                    IPFSWrapper.this.IPFSProcess.destroy();
                } catch (Exception bad) 
                {
                	Log.error("Couldn't stop IPFS!", bad);
                }
            }
        });
	}
	
	
	// Returns true if the user agrees
	private boolean getUserConsentToStartIPFS()
		throws IOException
	{
        String userDir = OSUtil.getSettingsDirectory();
        File ipfsMessageFlagFile = new File(userDir + File.separator + "ipfsInfoShown.flag");
        if (ipfsMessageFlagFile.exists())
        {
            return true;
        } 
		
		Object[] options = { "Yes", "No", "Yes and do not show this message again" };

		int option = JOptionPane.showOptionDialog(
			this.parentFrame, 
			"This operation will start an IPFS server on your PC to enable file sharing.\n"      +
			"As a result your PC will become a node in the Inter-Planetary File System that\n"   +
			"enables distributed sharing of information. Before proceeding with IPFS, please\n"  +
			"make sure you understand the full implications of this by getting familiar with\n" +
			"the details of IPFS at this web site: https://ipfs.io/\n" +
			"\n" +
			"Do you wish to start an IPFS server on your PC?", 
			"Confirm starting an IPFS server...",
			JOptionPane.DEFAULT_OPTION, 
			JOptionPane.INFORMATION_MESSAGE,
			null, 
			options, 
			options[0]);
		
	    if (option == 2)
	    {
	    	ipfsMessageFlagFile.createNewFile();
	    }
		
	    return (option != 1);
	}
	
	
	private boolean isIPFSWrapperRunning()
		throws IOException, InterruptedException
	{
		DaemonInfo info = this.getIPFSDaemonInfo();
		
		return info.status == DAEMON_STATUS.RUNNING;
	}
	
	
	private DaemonInfo getIPFSDaemonInfo()
		throws IOException, InterruptedException
	{
		OS_TYPE os = OSUtil.getOSType();
		
		if (os == OS_TYPE.WINDOWS)
		{
			return ZCashInstallationObserver.getDaemonInfoForWindowsOS("ipfs");
		} else
		{
			return ZCashInstallationObserver.getDaemonInfoForUNIXLikeOS("ipfs");
		}
	}
	
	
	private String getIPFSFullExecutablePath()
		throws IOException
	{
		return this.getIPFSDirectory() + File.separator + this.getIPFSExecutableName();
	}
	
	
	private String getIPFSDirectory()
		throws IOException
	{
		String walletBase = OSUtil.getProgramDirectory();
		
		return walletBase + File.separator + "go-ipfs";
	}
	
	
	private String getIPFSExecutableName()
	{
		String ipfs = "ipfs";		
		OS_TYPE os = OSUtil.getOSType();
		if (os == OS_TYPE.WINDOWS)
		{
			ipfs += ".exe";
		}
		
		return ipfs;
	}
		
}