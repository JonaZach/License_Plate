## Description:
A system that informs whether vehicles are allowed to enter a parking lot by determining the vehicle's type, using public API OCR

## Instructions:
1. Clone/Download the repository to your computer
2. Navigate to the project root directory (where pom.xml is located) using the terminal
3. Run `mvn install` - this will build the project and run the unit tests automatically
4. Run `mvn spring-boot:run`, this will start the service
5. When you see a meesage like `Started Application in 2.034 seconds`, the service is running and can be accessed at http://localhost:8080/

## Endpoints:

### GET plates/list
	List of all the vehicles which tried to enter the parking lot
### GET plates/check?image_url=URL
	Replace "URL" with the URL of the license plate's image you wish to test

## Images:
These are the images I have used in the unit tests:
* https://i.ibb.co/41Q5wj7/01234589.jpg
* https://i.ibb.co/zsmpbmL/7722286.jpg
* https://i.ibb.co/qCPGnQm/00.png
* https://i.ibb.co/xSX7w0j/12.jpg
* https://i.ibb.co/SmqQrjF/2952165.jpg
* https://i.ibb.co/tcr0S7n/2026022.png
* https://i.ibb.co/gR9jkg6/cali.jpg
* https://i.ibb.co/Q8RPWBc/9.jpg
	
