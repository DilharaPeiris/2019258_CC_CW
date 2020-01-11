# AWS Cloud Formation with Jmeter testing

# Create yml file in Cloud Formation
	1. Drag and drop resources and Methods
	2.Then add Lambda fucntion and VPCs.
	3.Deploy the stack

# Check API Gateway
	If API Gateway has created API on the name you have given then the stack has been deoployed successfuly.
	Click on the API gateway and click stages. and Check the end points are working using browser or postman. 
	

If it is working properly testing through Jmeter can be do. 

# Jmeter Testing
	1. Execute jmeter.bat and direct to the jmeter UI.
	2. Add TestPlan --> Add Threads --> Add Thread Group
	3. Add HTTPRequest under the sampler.
	4. As shows in image in report add the server name or IP then path
	5. If there's any parameters add parameters with their name and value. 
	6. Run the jmax script. 
	7. Try changing Ramp up period and Loop count and test the HTTP methods that has been created.
	
