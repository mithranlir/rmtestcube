package com.qfs.lic.impl;

import com.qfs.lic.IQuartetFSLicense;
import com.qfs.logging.MessagesDatastore;
import com.qfs.platform.IPlatform;
import com.quartetfs.activation.impl.LicenseException;
import com.quartetfs.activation.impl.LicenseManager;
import com.quartetfs.activation.impl.Platform;
import com.quartetfs.biz.pivot.impl.Util;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Licensing
{
    static final Logger logger = MessagesDatastore.getLogger(Licensing.class);
    private static final String newLine = System.getProperty("line.separator");
    static final AtomicBoolean infoLogged = new AtomicBoolean(false);
    private static volatile LicenseContext licenseContext;
    private static volatile QuartetFSLicense license;

    public static void logLicenseInfoOnce()
    {
        logger.log(Level.INFO, "COUCOU");
        if (infoLogged.compareAndSet(false, true)) {
            logger.log(Level.INFO, getLicenseContext().toString() + "\n" + getLicense().toString());
        }
    }

    public static boolean checkLicence()
    {
        logLicenseInfoOnce();

        boolean isValid = getLicenseContext().verifyLicense(getLicense());
        logger.log(Level.INFO, "License status: " + (isValid ? "Valid" : "Invalid"));
        logger.log(Level.INFO, "License: " + getLicense().getLicenseStatus());
        return isValid;
    }

    private static final LicenseContext getLicenseContext()
    {
        if (licenseContext == null) {
            synchronized (LicenseContext.class)
            {
                if (licenseContext == null) {
                    licenseContext = new LicenseContext();
                }
            }
        }
        return licenseContext;
    }

    private static final class LicenseContext
    {
        final long currentTime;
        final int cpuCount;

        private LicenseContext()
        {
            this.currentTime = System.currentTimeMillis();
            this.cpuCount = IPlatform.CURRENT_PLATFORM.getProcessorCount();
        }

        private boolean verifyLicense(Licensing.QuartetFSLicense license)
        {
            if ((this.currentTime > license.getEndDate()) || (this.currentTime < license.getStartDate()))
            {
                Licensing.logger.log(Level.SEVERE, "EXC_EXPIRED_LICENSE");
                String status = "Your license has expired: system date (" + new Date(this.currentTime) + ") is not between start date (" + new Date(license.getStartDate()) + ") and end date (" + new Date(license.getEndDate()) + ")." + Licensing.newLine;
                status = status + "Please contact Quartet FS.";
                Licensing.getLicense().status = status;
                return false;
            }
            boolean restrictionFound = false;
            if (!"-".equals(license.getMac()))
            {
                restrictionFound = true;
                String mac;
                try
                {
                    mac = Platform.getLocalHostMacAddress();
                }
                catch (IOException e)
                {
                    Licensing.getLicense().status = "Unable to obtain MAC Address because of IOException.";
                    Licensing.logger.log(Level.SEVERE, Licensing.getLicense().getLicenseStatus(), e);
                    return false;
                }
                if ((mac == null) || (mac.isEmpty()))
                {
                    Licensing.getLicense().status = "Unable to obtain MAC Address (empty MAC Address).";
                    Licensing.logger.log(Level.SEVERE, Licensing.getLicense().getLicenseStatus());
                    return false;
                }
                if (!Platform.equals(mac, license.getMac()))
                {
                    Licensing.logger.log(Level.SEVERE, "EXC_MAC_LICENSE", new Object[] { mac, license.getMac() });
                    Licensing.getLicense().status = ("Your license is bound to another machine (your MAC Address: " + mac + ", license MAC Address: " + license.getMac() + ").");
                    return false;
                }
            }
            if (!"-".equals(license.getIp()))
            {
                restrictionFound = true;
                String ip;
                try
                {
                    ip = Platform.getLocalHostAddress();
                }
                catch (UnknownHostException e)
                {
                    Licensing.logger.log(Level.SEVERE, "EXC_UNKNOWN_IP", e);
                    Licensing.getLicense().status = "EXC_UNKNOWN_IP";
                    return false;
                }
                if (!ip.equals(license.getIp()))
                {
                    Licensing.logger.log(Level.SEVERE, "EXC_IP_LICENSE");
                    Licensing.getLicense().status = ("Your license is bound to another machine (your IP Address: " + ip + ", license IP Address: " + license.getIp() + ").");
                    return false;
                }
            }
            if (!"-".equals(license.getHostname()))
            {
                restrictionFound = true;
                String hostname;
                try
                {
                    hostname = Platform.getLocalHostName();
                }
                catch (UnknownHostException e)
                {
                    Licensing.logger.log(Level.SEVERE, "EXC_UNKNOWN_HOST", e);
                    Licensing.getLicense().status = "EXC_UNKNOWN_HOST";
                    return false;
                }
                if (!hostname.equalsIgnoreCase(license.getHostname()))
                {
                    Licensing.logger.log(Level.SEVERE, "EXC_HOST_LICENSE", new Object[] { hostname, license.getHostname() });
                    Licensing.getLicense().status = ("Your license is bound to another machine (your hostname: " + hostname + ", license hostname: " + license.getHostname() + ").");
                    return false;
                }
            }
            if (!restrictionFound)
            {
                Licensing.logger.log(Level.SEVERE, "EXC_UNRESTRICTED_LICENSE");
                String status = "The license is not valid due to violated restrictions." + Licensing.newLine;
                status = status + "Please contact the QuartetFS support team.";
                Licensing.getLicense().status = status;
                return false;
            }
            if (this.cpuCount > license.getCpuCount())
            {
                Licensing.logger.log(Level.SEVERE, "EXC_CORE_EXCEEDED_LICENSE", new Object[] { Integer.valueOf(license.getCpuCount()), Integer.valueOf(this.cpuCount) });
                String status = "The number fo cores allowed by the license is not enough to operate on this server." + Licensing.newLine;
                status = status + "Cores allowed by the license: " + license.getCpuCount() + Licensing.newLine;
                status = status + "Cores in the server: " + this.cpuCount + Licensing.newLine;
                status = status + "Please contact the QuartetFS support team.";
                Licensing.getLicense().status = status;
                return false;
            }
            Licensing.getLicense().status = "License loaded and tested successfully.";
            return true;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("\n*************** Plaftorm Information *****************");

            sb.append("\nSystem Time: ");
            sb.append(new Date(this.currentTime));

            sb.append("\nNumber of Processors: ");
            sb.append(this.cpuCount);
            String mac;
            try
            {
                mac = Platform.getLocalHostMacAddress();
            }
            catch (IOException e)
            {
                mac = "Error retrieving MAC address";
            }
            sb.append("\nMAC address: ");
            sb.append(mac);
            String ip;
            try
            {
                ip = Platform.getLocalHostAddress();
            }
            catch (UnknownHostException e)
            {
                ip = "Error retrieving IP address";
            }
            sb.append("\nIP address: ");
            sb.append(ip);
            String hostname;
            try
            {
                hostname = Platform.getLocalHostName();
            }
            catch (UnknownHostException e)
            {
                hostname = "Error retrieving hostname";
            }
            sb.append("\nHostname: ");
            sb.append(hostname);

            sb.append("\n******************************************************");

            return sb.toString();
        }
    }

    public static QuartetFSLicense createFakeQuartetFSLicense() {
        final Properties properties = new Properties();

        final Long startDate = (new Date()).getTime() - (24 * 60 * 60 * 1000); // now - 1d;
        properties.put("sDate", startDate.toString());

        final Long endDate = (new Date()).getTime() + (365 * 24 * 60 * 60 * 1000); // now + 1 year;
        properties.put("eDate", endDate.toString());

        String host = "";
        try {
            host = Platform.getLocalHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        properties.put("host", host);

        final int maxNbOfCPU = Util.retrieveNumberOfAllowedCores();
        properties.put("maxNbOfCPU", maxNbOfCPU + "");

        properties.put("live", "YES");

        properties.put("sentinel", "YES");

        properties.put("rs", "YES");

        final QuartetFSLicense license = new QuartetFSLicense(properties);

        license.status = "License loaded and tested successfully.";

        return license;
    }

    public static final QuartetFSLicense getLicense()
    {
        if (license == null) {
            synchronized (QuartetFSLicense.class)
            {
                if (license == null)
                {
                    logger.log(Level.INFO, "Quartet FS ActivePivot Licensing Agent.");
                    license = createFakeQuartetFSLicense();
                }
            }
        }
        return license;
    }

    public static final byte[] getBinaryLicense()
    {
        return null;
    }

    public static final void reset()
    {
        if (license != null) {
            synchronized (QuartetFSLicense.class)
            {
                if (license != null)
                {
                    logger.log(Level.INFO, "Resetting Quartet FS Licensing Agent.");
                    license = null;
                }
            }
        }
    }

    public static final class QuartetFSLicense
            implements IQuartetFSLicense
    {
        public static final String LICENSE_FILENAME = "ActivePivot.lic";
        public static final String LICENSE_SYSTEM_PROPERTY = "activepivot.license";
        public static final String LICENSE_ENVIRONMENT_VARIABLE = "ACTIVEPIVOT_LICENSE";
        final Properties properties;
        String status = "No license has been loaded";

        QuartetFSLicense()
        {
            this(new Properties());
        }

        public QuartetFSLicense(Properties properties)
        {
            this.properties = properties;
        }

        public String getId()
        {
            return this.properties.getProperty("id", "-");
        }

        public long getStartDate()
        {
            return Long.parseLong(this.properties.getProperty("sDate", "0"));
        }

        public long getEndDate()
        {
            return Long.parseLong(this.properties.getProperty("eDate", "0"));
        }

        public String getMac()
        {
            return this.properties.getProperty("mac", "-");
        }

        public String getIp()
        {
            return this.properties.getProperty("ip", "-");
        }

        public String getHostname()
        {
            return this.properties.getProperty("host", "-");
        }

        public int getCpuCount()
        {
            return Integer.parseInt(this.properties.getProperty("maxNbOfCPU", "0"));
        }

        public String getEnv()
        {
            return this.properties.getProperty("env", "-");
        }

        public boolean getLiveEnabled()
        {
            return "YES".equalsIgnoreCase(this.properties.getProperty("live"));
        }

        public boolean getSentinelEnabled()
        {
            return "YES".equalsIgnoreCase(this.properties.getProperty("sentinel"));
        }

        public boolean getDatastoreEnabled()
        {
            return "YES".equalsIgnoreCase(this.properties.getProperty("rs"));
        }

        public String getLicenseStatus()
        {
            return this.status;
        }

        public int hashCode()
        {
            return this.properties.hashCode();
        }

        public boolean equals(Object other)
        {
            if (this == other) {
                return true;
            }
            if ((other == null) || (!QuartetFSLicense.class.equals(other.getClass()))) {
                return false;
            }
            QuartetFSLicense license = (QuartetFSLicense)other;
            return this.properties.equals(license.properties);
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("\n********** ActivePivot License Information ***********\n");

            sb.append("License ID: ");
            sb.append(getId());

            sb.append("\nStart Date: ");
            sb.append(new Date(getStartDate()));

            sb.append("\nEnd Date: ");
            sb.append(new Date(getEndDate()));

            sb.append("\nMaximum Number of Processors: ");
            sb.append(getCpuCount());

            sb.append("\nRestricted MAC address: ");
            sb.append(getMac());

            sb.append("\nRestricted IP address: ");
            sb.append(getIp());

            sb.append("\nRestricted Hostname: ");
            sb.append(getHostname());

            sb.append("\nTarget Environment: ");
            sb.append(getEnv());

            sb.append("\nDatastore Enabled: ");
            sb.append(getDatastoreEnabled());

            sb.append("\nActivePivot Live Enabled: ");
            sb.append(getLiveEnabled());

            sb.append("\nActivePivot Sentinel Enabled: ");
            sb.append(getSentinelEnabled());

            sb.append("\n******************************************************");
            sb.append("\n");

            return sb.toString();
        }
    }
}
