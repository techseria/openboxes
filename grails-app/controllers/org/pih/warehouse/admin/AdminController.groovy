/**
* Copyright (c) 2012 Partners In Health.  All rights reserved.
* The use and distribution terms for this software are covered by the
* Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
* which can be found in the file epl-v10.html at the root of this distribution.
* By using this software in any fashion, you are agreeing to be bound by
* the terms of this license.
* You must not remove this notice, or any other, from this software.
**/ 
package org.pih.warehouse.admin

import grails.util.GrailsUtil
import grails.util.Holders
import org.pih.warehouse.core.MailService
import org.springframework.cache.Cache
import org.springframework.web.multipart.MultipartFile

import javax.print.Doc
import javax.print.DocFlavor
import javax.print.DocPrintJob
import javax.print.PrintService
import javax.print.SimpleDoc
import java.awt.print.PrinterJob

import java.util.concurrent.FutureTask;

class AdminController {

    def sessionFactory // inject Hibernate sessionFactory
	MailService mailService;
	def getGrailsApplication = Holders
	def config = Holders.config
    def quartzScheduler
	def springcacheService

	def index = { }


    def controllerActions = {
			
		List actionNames = []
        getGrailsApplication.controllerClasses.sort { it.logicalPropertyName }.each { controller ->

			controller.reference.propertyDescriptors.each { pd ->
				def closure = controller.getPropertyOrStaticPropertyOrFieldValue(pd.name, Closure)
				if (closure) {
					if (pd.name != 'beforeInterceptor' && pd.name != 'afterInterceptor') {
						actionNames << controller.logicalPropertyName + "." + pd.name + ".label = " + pd.name
					}
				}
			}
			println "$controller.clazz.simpleName: $actionNames"
		}
		
		[actionNames:actionNames]
	}

    def cache = {
        [cacheStatistics: sessionFactory.getStatistics()]
    }

    def plugins = { } 
    def status = { } 
    
	def static LOCAL_TEMP_WEBARCHIVE_PATH = "warehouse.war"
	
	def showUpgrade = { UpgradeCommand command ->
		log.info "show upgrade " + params
		
		[
			command : session.command
			//remoteFileSize: getRemoteFileSize(command?.remoteWebArchiveUrl),
			//remoteFileLastModifiedDate: new Date(getRemoteFileLastModifiedDate(command?.remoteWebArchiveUrl)) 
		]				
	}

    def evictDomainCache = {
        def domainClass = getGrailsApplication.getDomainClass(params.name)
        if (domainClass) {
            sessionFactory.evict(domainClass.clazz)
            flash.message = "Domain cache '${params.name}' was invalidated"
        }
        else {
            flash.message = "Domain cache '${params.name}' does not exist"
        }
        redirect(action: "showSettings")
    }

    def evictQueryCache = {
        if (params.name) {
            sessionFactory.evictQueries(params.name)
            flash.message = "Query cache '${params.name}' was invalidated"
        }
        else {
            sessionFactory.evictQueries()
            flash.message = "All query caches were invalidated"
        }
        redirect(action: "showSettings")
    }


	def sendMail = {

        println "sendMail: " + params

        if (request.method == "POST") {
            try {
                withForm {
                    MultipartFile multipartFile = request.getFile('file')
                    if (!multipartFile.empty) {
                        byte[] bytes = multipartFile.bytes

                        println multipartFile.contentType
                        println multipartFile.originalFilename
                        println multipartFile.name

                        def emailMessageMap = [
                                from: session?.user?.email,
                                to: params.list("to"),
                                cc: [],
                                bcc: [],
                                subject: params["subject"],
                                body: params["message"],
                                attachment: multipartFile?.bytes,
                                attachmentName: multipartFile?.originalFilename,
                                mimeType: multipartFile?.contentType
                        ]
                        mailService.sendHtmlMailWithAttachment(emailMessageMap);
                        flash.message = "Multipart email with subject ${params.subject} and attachment ${multipartFile.originalFilename} has been sent to ${params.to}"
                    } else {
                        if (params.includeHtml) {
                            mailService.sendHtmlMail(params.subject, params.message, params.to)
                            flash.message = "HTML email with subject ${params.subject} has been sent to ${params.to}"
                        } else {
                            mailService.sendMail(params.subject, params.message, params.to)
                            flash.message = "Text email with subject ${params.subject} has been sent to ${params.to}"
                        }
                    }
                }.invalidToken {
                    flash.message = "Invalid token"
                }
            } catch (Exception e) {
                flash.message = "Unable to send email due to error: " + e.message
            }
        }


    }

	
	def download = { UpgradeCommand command ->
		log.info "download " + params
		if (command?.remoteWebArchiveUrl) {
			session.command = command
			session.command.future = null
			session.command?.localWebArchive = new File("warehouse.war")								
			flash.message = "Attempting to download '" + command?.remoteWebArchiveUrl + "' to '" + command?.localWebArchive?.absolutePath + "'"
			// Requires executor plugin
			//session.command.future = callAsync {			
			//	return doDownloadWar(command?.remoteWebArchiveUrl, command?.localWebArchive)
			//}
		}
		else {
			flash.message = "Please enter valid web archive url";
			
		}
		
		chain(action: "showUpgrade", model: [command : command])
		//redirect (action: "showUpgrade")
	}
	
	
	def deploy = { UpgradeCommand command -> 
		log.info "deploy " + params
		
		session.command.localWebArchivePath = command.localWebArchivePath
		command.localWebArchive = session.command.localWebArchive
	
		def source = session.command.localWebArchive		
		def destination = new File(session.command.localWebArchivePath)		
		def backup = new File(session.command.localWebArchive.absolutePath + ".backup")
		log.info "Copying wbe archive to backup " + source.absolutePath + " to " + backup.absolutePath
		backup.bytes = source.bytes
		
		log.info "Copying web archive to web container " + destination.absolutePath 
		destination.bytes = source.bytes	
		
		chain(action: "showUpgrade", model: [command : command])
		//redirect (view: "showUpgrade", model: [command: command])
	}


	def testZebraPrinter = {
        try {

            PrintService[] printServices = PrinterJob.lookupPrintServices();
            //PrinterJob
            DocPrintJob job = psZebra.createPrintJob();

            String s = "N" + "\n" +
                    "q305" + "\n" +
                    "Q203,26" + "\n" +
                    "B55,26,0,1,2,2,152,B,\"" + code + "\"" + "\n" +
                    "P1,1";

            InputStream inputStream = new ByteArrayInputStream(s.getBytes());
            DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
            Doc doc = new SimpleDoc(inputStream, flavor, null);

            job.print(doc, null);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	
	def showSettings = { 		
		def externalConfigProperties = []
        Holders.config.grails.config.locations.each { filename ->
			try { 
				// Hack to remove the file: protocol from the URL string
				filename -= "file:"
				def file = new File(filename)
				def inputStream = new FileInputStream(file)
				def properties = new Properties()
				properties.load(inputStream)
				externalConfigProperties << properties
			} catch (FileNotFoundException e) { 
				log.warn("Properties file not found: " + e.message)
			}
		}

		PrintService[] printServices = PrinterJob.lookupPrintServices();

        def caches = new ArrayList()
        def cacheNames = springcacheService.springcacheCacheManager.cacheNames

        for (cacheName in cacheNames) {
            Cache cache = springcacheService.springcacheCacheManager.getCache(cacheName)
            if (cache instanceof Cache) {
                caches.add(cache)
            }
        }


                [
            quartzScheduler:quartzScheduler,
			printServices:printServices,
            caches: caches,
			externalConfigProperties: externalConfigProperties,
			systemProperties : System.properties,
			env: GrailsUtil.environment,
			enabled: Boolean.valueOf(Holders.config.grails.mail.enabled),
			from: "${config.grails.mail.from}",
			host: "${config.grails.mail.host}",
			port: "${config.grails.mail.port}"
		]
	}
		
	
	def downloadWar = { 
		log.info params
		println("Updating war file " + params)
		// def url = "http://ci.pih-emr.org/downloads/openboxes.war"

		// Requires executor plugin
		//def future = callAsync {
		//	return doDownloadWar(url) 
		//}		
		//session.future = future		
		redirect(action: "showSettings")
	}
	
	def cancelUpdateWar = { 
		log.info params
		if (session.future) { 
			session.future.cancel(true)
			new File(LOCAL_TEMP_WEBARCHIVE_PATH).delete()
		}
		redirect(action: "showSettings")
	}
		
	def deployWar = { UpgradeCommand -> 
		log.info params
		def source = session.command.localWebArchive
		
		// def destination = new File(session.command.localWebArchivePath)
		
		def backup = new File(session.command.localWebArchive.absolutePath + ".backup")
		log.info "Backing up " + source.absolutePath + " to " + backup.absolutePath 
		backup.bytes = source.bytes

		//destination.bytes = source.bytes
		
		
		redirect(action: "showSettings")
	}

	Integer doDownloadWar(String remoteUrl, File localFile) { 
		try { 
			println("Downloading war file " + remoteUrl + " .... ")
			def outputStream = new BufferedOutputStream(new FileOutputStream(localFile))		
			def url = new URL(remoteUrl)
			outputStream << url.openStream()
			outputStream.close();
			println("... done downloading remote file " + remoteUrl + " to " + localFile.absolutePath)
			//return file.absolutePath
			return 0
		} catch (Exception e) { 
			log.error e
			throw e;
		}
	}
	
	
	
	/*
	def reloadWar = {
		println("Reloading war file")
		def future = callAsync {
			log.info "Within call async"
			return reloadWar()
		}
		session.future = future
		redirect(action: "showSettings")
	}
	
	def doReloadWar() { 
		def connection = null
		try {
			//Create connection
			def url = new URL("http://localhost:8180/manager/reload?path=/openboxes");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = "tomcat" + ":" + "tomcat";
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
			connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			return connection.content.text
			
		} catch (Exception e) {
			log.error e
			render e
		} finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
	}
	*/
	
	/*
	def getApplications() { 
		def applications = []		
		def connection = null
		try {
			//Create connection
			def url = new URL("http://localhost:8180/manager/list");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			BASE64Encoder enc = new sun.misc.BASE64Encoder();
			String userpassword = "tomcat" + ":" + "tomcat";
			String encodedAuthorization = enc.encode( userpassword.getBytes() );
			connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);

			connection.content.text.eachLine { 
				applications << it.tokenize(":")
			}
			
		} catch (Exception e) {
			log.error "test"

		} finally {

			if(connection != null) {
				connection.disconnect();
			}
		}
		return applications 
	}
	*/



}

class UpgradeCommand {
	
	FutureTask future	
	File localWebArchive
	String remoteWebArchiveUrl
	String localWebArchivePath

	static constraints = {
		future(nullable:true)
		localWebArchive(nullable:true)
		remoteWebArchiveUrl(nullable: true)
		localWebArchivePath(nullable: true)
	}

	
	Integer getRemoteFileSize() {
		if (remoteWebArchiveUrl) { 
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) new URL(remoteWebArchiveUrl).openConnection();
				conn.setRequestMethod("HEAD");
				conn.getInputStream();
				return conn.getContentLength();
			} catch (IOException e) {
				return -1;
			} finally {
				if (conn) conn.disconnect();
			}
		}
		return -1;
	}
	
	Date getRemoteFileLastModifiedDate() {
		if (remoteWebArchiveUrl) { 
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) new URL(remoteWebArchiveUrl).openConnection();
				conn.setRequestMethod("HEAD");
				conn.getInputStream();
				return new Date(conn.getLastModified());
			} catch (IOException e) {
				return null;
			} finally {
				if (conn) conn.disconnect();
			}
		}
		return null;		
	}
	
	
	Float getProgressPercentage() { 
		def remoteFileSize = getRemoteFileSize()
		def localFileSize = localWebArchive?.size()
		if (remoteFileSize > 0 && localFileSize > 0) { 
			return (localFileSize / remoteFileSize) * 100
		}
		return -1;
	}
	
}