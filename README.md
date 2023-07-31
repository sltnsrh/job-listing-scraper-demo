## Job Listing Scraper
___

The Job Listing Scraper is an application that allows users to efficiently search and gather job listings 
from the jobs.techstars.com platform based on specific job functions. Users can choose their desired work functions 
or job categories, and the application will automatically crawl the website to collect relevant job postings 
for those functions.
Also, users can search by keyword in position name, organizations title, labour function, locations, descriptions, and tags.
The scraped data is presented in a user-friendly json format, providing key details such as job title, 
organizations title, locations, description, posting date, etc. Data will be sorted by desired field and direction, and 
represent in pageable format. Saving job listings to a local MySQL database for easy access and reference.
___
### Usage:

There is one API endpoint: `GET: /jobs/search` <br />
Available request parameters: 
* jobFunction
  * Accounting & Finance
  * Administration
  * Customer Service
  * Data Science
  * Design
  * IT 
  * Legal
  * Marketing & Communications
  * Operations
  * Other Engineering
  * People & HR
  * Product
  * Quality Assurance
  * Sales & Business Development
  * Software Engineering
* page
* size (default size 10)
* sortBy (default sorting by positionName)
* direction (default direction ASC)
* findByField
* keyword
___
### Importantly
Currently, the app has a problem with the stability of loading large amounts of data. That's why the temporary scraping 
scope is limited to 20 pages from jobs.techstars.com. In case any data will not be loaded, retry the request a few times.
It needs to be fixed.
___

**App was updated** on 31.07.23. Rebuilt with using Selenium WebDriver and fixed limitation on pages loading. New dump file

added to project folder here: [sofware-engineering.sql](sofware-engineering.sql)
Also you can load it from Google Drive: https://drive.google.com/file/d/1wg7T0RfRlbVpNlQ9pzwmxBmEdyIT6T4S/view?usp=sharing