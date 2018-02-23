Prerequisites: You should have following setup on your machine

    a. Apache OFBiz instance running with 16.11 and 17.12 release (for more details refer: http://ofbiz.apache.org/index.html)

    b. A Magento instance with a Community or Enterprise edition (for more details please refer to http://www.magentocommerce.com/knowledge-base/entry/ce18-and-ee113-install-upgrade)


There are four easy steps to use the 'Magento' component with Apache OFBiz.

1. Use the OFBizConnect source code available at https://github.com/hotwaxlabs/OFBizConnect.git

   a. Copy the OFBizConnect/app/code/community/OFBiz folder into the Magento installation directory app/code/community/

   b. Copy the OFBizConnect/app/etc/modules/OFBiz_Magentoext.xml into the Magento installation directory app/etc/modules/

   c. Sign into the Magento store admin panel and flush the Magento cache by clicking on 'Flush Magento Cache' button on system -> cache management page

                        OR

1. Upload the plugin to Magento instance

   a. Download the plugin file(OFBiz_Connect-1.0.0.tgz) attached on OFBIZ jira ticket OFBIZ-6236(https://issues.apache.org/jira/browse/OFBIZ-6236).

   b. Follow the below steps to upload the plugin on Magento instance

        -- Sign into the Magento store admin panel.
        -- Upload the plugin file(OFBiz_Connect-1.0.0.tgz) from 'Direct package file upload' section of page.
        -- Flush Magento cache by clicking on 'Flush Magento Cache' button on system -> cache management page

2. Create SOAP users and roles in Magento.

   These users will be used by the OFBiz integration services to establish connection with Magento. Please follow these steps:

   a. Create Role

       -- Login into the Admin application of Magento and go to the Dashboard screen (http://magentohost/admin/)

       -- Go to System => Web Services => SOAP/XML-RPC – Roles

       -- Click on  "Add New Role" button.

       -- Fill Role Name and save the Role

       -- Go to the "Role Resources" tab, under "Role Information" section. Select "All" from "Resource Access" drop down for giving access to all the resources and save by clicking "Save Role" button.
   
  b. Create User

       -- Go to System => Web Services => Soap/XML-RPC – Users

       -- Click on "Add New User" button and fill form details.

       -- Go to the "User Role" tab, under "User Information" section and select the role which you have created in above steps and click on "Save User" button.

3. Do WS-I Compliance Related Settings

       -- Login into the Admin application of Magento and go to Dashboard screen (http://magentohost/admin/)

       -- Go to System => Configuration => Services => Magento Core API and set 'WS-I Compliance' and 'Enable WSDL Cache' fields to Yes. 

4. Configure OFBiz
    
       -- Check out the magento component from github: https://github.com/hotwaxlabs/magento.git and put it in hot-deploy/specialpurpose directory of OFBiz

       -- Load seed, seed-initial and ext data readers of the magento component. Please run the command as: ./ant load-readers -Ddata-readers=seed,seed-initial,ext

       -- Go to the configuration screen of OFBiz Integration Manager https://ofbizhost/magento/control/main and login. Here, You just need to add url of Magento instance as pattern shown in example along with username and password of SOAP user created in Magento.

       -- After submitting the configuration page, click on Import button to fetch the available store settings from the Magento instance.

       -- Finally you will see the Integration Manager screen with all fetched information. You Just need to follow the page section instructions.
  
By following the above mentioned steps you can easily setup the Magento-OFBiz Integration. Once you are done with configuration, simply schedule the services by loading JobSandbox data from file "MagentoScheduleServiceData.xml"

To know more about this integration, please feel free to get in touch with us at http://www.hotwaxsystems.com/contact-us/

