Prerequisites: You should have following setup on your machine

Apache OFBiz instance running with 13.07 or later release (for more details refer: http://ofbiz.apache.org/index.html)
 A Magento instance with Community or Enterprise edition (for more details please refer to http://www.magentocommerce.com/knowledge-base/entry/ce18-and-ee113-install-upgrade)

There are four easy steps to use 'Magento' component with your Apache OFBiz.

1.Use OFBizConnect source code available at https://github.com/hotwaxlabs/OFBizConnect.git
    a. Copy OFBizConnect/app/code/community/OFBiz folder into your Magento installation directory app/code/community/
    b. Copy OFBizConnect/app/etc/modules/OFBiz_Magentoext.xml into your Magento installation directory app/etc/modules/
    c. Sign into your Magento store admin panel and flush Magento cache by clicking on 'Flush Magento Cache' button on system -> cache management page
                         OR
1. Upload plugin to Magento instance
    a. Download plugin file(OFBiz_Connect-1.0.0.tgz) attached on OFBIZ jira ticket OFBIZ-6236 and soon it will be available in Magento Market place
    b. Follow below steps to upload plugin on Magento instance
       --Sign into your Magento store admin panel.
       – Navigate to System > Magento Connect > Magento Connect Manager.
       – Upload plugin file(OFBiz_Connect-1.0.0.tgz) from 'Direct package file upload' section of page.
       – Return back to admin panel
       – Flush Magento cache by clicking on 'Flush Magento Cache' button on system -> cache management page

2. Create SOAP users and roles in Magento
   This user will be used by OFBiz integration services to establish connection with Magento. Please follow
  a) Create Role
     -Login into the Admin application of Magento and go to Dashboard screen (http://magentohost/admin/)
     -Go to System => Web Services => SOAP/XML-RPC - Roles
     -Click on  "Add New Role" button.
     -Fill Role Name and save the Role
     -Go to "Role Resources" tab, under "Role Information" section. Select "All" from "Resource Access" drop down for giving access to all the resources and save by clicking "Save Role" button.
    
   b) Create User
     -Go to System => Web Services => Soap/XML-RPC - Users
     -Click on "Add New User" button and fill form details.
     -Go to "User Role" tab, under "User Information" section and select the role which you have created in above steps under (A) and click on "Save User" button.

3. Do WS-I Compliance Related Settings
   -Login into the Admin application of Magento and go to Dashboard screen (http://magentohost/admin/)
   -Go to System => Configuration => Services => Magento Core API and set 'WS-I Compliance' and 'Enable WSDL Cache' fields to Yes. 

4. Configure OFBiz
    Now you have all details that need to configure in OFBiz for accessing Magento webservices.
   -Check out magento component from github and put it in hot-deploy of OFBiz
   -Load seed, ext-seed and ext data of magento component
   -Go to configuration screen of OFBiz Integration Manager https://ofbizhost/magento/control/main and login. Here, You just need to add url of Magento instance in predefined pattern and username and password of SOAP user created in Magento.
   -After submitting configuration page, you see next screen as below. Click on Import button to get fetch settings and information from Magento instance so that you will no need to do it again in OFBiz.
   -Finally you will see screen with all fetched information. You Just need to follow it as per requirement.
   
By following the above mentioned steps in the sequence you can easily setup the Magento-OFBiz Integration. OFBiz has been successful as external e-commerce stores like Magento.
