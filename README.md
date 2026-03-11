PART 1 – Customer Service Bot
This project implements a customer service chatbot that helps users retrieve information quickly from a knowledge base and backend systems. The chatbot allows customers to ask questions through a chat interface and receive responses related to application status, failed card transactions, and support information available in the knowledge base.
The system processes user queries and determines whether the request should be answered using the knowledge base link or backend service data. If the query relates to general support information, the chatbot searches the knowledge base and returns the relevant response. For queries related to application status or transaction failures, the system retrieves the required information from backend services and presents it to the user.
The chatbot is integrated with a backend service (built using Spring Boot) and provides a simple chat interface for user interaction, making it easier for customers to get answers to common queries efficiently.

Here are some validation screenshots:
1.	Run spring boot and front end application. 
2.	Launch the application using http://localhost:5173/
3.	When asked about application status, it will pull the status from backend. 
 
4.	When asked about failed transaction it would give the status of transaction.
 
5.	Now when asked about queries in knowledge base then response is from the URL of knowledge base. 
 

6.	Knowledge base URL can be updated in admin tab
Before change of URL: 
 

After URL change and we need to rebuild kb index to see the change in bot response.
 
 

7.	Now we can see difference in chat tab 
 

8.	Do report a mistake to change the response of bot.
After submitting report a mistake we can see the report in report menu. 
 

9.	Apply fix will make the bot respond with corrected answer. 
 
