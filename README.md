OpenIO SDS API
==============

OpenIO SDS API is a java remote API for [OpenIO Software Defined Storage](https://github.com/open-io/oio-sds). It is designed fully asynchronous, 
allow you to deal with container and object inside your namespace and is also pretty easy to use.

Getting started
------------

  OpenIO SDS API entry point is the Client implementations classes. Instances of these implementations
  are built with ClientBuilder class. You could create a basic client by calling
  ClientBuilder#newClient(String)} method, specifying the OpenIO proxyd
  service url as argument. If you don't know what is proxyd service, please refer to OpenIO SDS documentation [here](https://github.com/open-io/oio-sds/wiki/OpenIO-SDS-Proxy).
  The client built from ClientBuilder are ready to be used. Let's see some basics examples.

###### Basic client instantiation
   
    Client client = ClientBuilder.newClient("OPENIO", "http://127.0.0.1:6002");
    
###### Tunable client implementation

  To enable more tunable client instance, you should specify some directives to
  your {@link ClientBuilder} For example if you want to provide your own
  AsyncHttpClient instance, you have to proceed as follow:
  
    AsyncHttpClient http = Dsl.asyncHttpClient(
       new DefaultAsyncHttpClientConfig.Builder()
             .setRequestTimeout(10000)
             .setHttpClientCodecMaxChunkSize(8192 * 4));
    Client client = ClientBuilder.prepareClient()
      .ns("OPENIO")
      .proxydUrl("http://127.0.0.1:6002")
      .http(http)
      .build();

###### OioUrl object

  Each methods of the api will ask you for an OioUrl instance. This class simplify methods signatures and make code cleaner.
  You will build it simply by calling OioUrl#url(String, String) for container url, i.e. to perform operations on container,
  and by calling OioUrl#url(String, String, String) for object url. 
  
  The first parameter is called "ACCOUNT". It define a storage space inside your SDS namespace. It could be used 
  for application space. If you don't care about that, just choose a account name and keep the same all the time.
  
  The second parameter is the container name. It is the space in which you futures objects will be created directly. 
  Its name is unique by account.
  
  And the last parameter is the object name, which is the identifier of your object inside a container, so, like container in account,
  an object name is unique inside a container.
  
  Container url example:
     
    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    
  Object url example:
  
    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");

###### Container creation example 

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    Future<ContainerInfo> f = client.createContainer(url, new CompletionListener<ContainerInfo>() {
       @Override
       public void onThrowable(Throwable t) {
             System.err.println("Container creation failure");
             e.printStackTrace();
       }
       
       @Override
       public void onResponse(ContainerInfo obj) throws Exception {
            System.out.println("Container created");
       }
    });
    
###### Upload an object from a File

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    File file = new File("MY_SAMPLE_FILE.txt");
    Future<ObjectInfo> f = client.putObject(url, 1024L, file, new CompletionListener<ObjectInfo>() {
          @Override
          public void onThrowable(Throwable t) {
                System.err.println("Object upload error");
                e.printStackTrace();
          }
          
          @Override
          public void onResponse(ObjectInfo obj) throws Exception {
               System.out.println("Object uploaded");
          }
       });

###### Upload an object from an InputStream

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    File file = new File("MY_SAMPLE_FILE.txt");
    FileInputStream fis = new FileInputStream(file);
    Future<ObjectInfo> f = client.putObject(url, 1024L, fis, new CompletionListener<ObjectInfo>() {
          @Override
          public void onThrowable(Throwable t) {
                System.err.println("Object upload error");
                e.printStackTrace();
          }
          
          @Override
          public void onResponse(ObjectInfo obj) throws Exception {
               System.out.println("Object uploaded");
          }
       });

###### Download an object

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    File out = new File("MY_FILE.out");
    OutputStream fos = new FileOutputStream(out);
    Future<Boolean> f = client.downloadObject(oinf,
       new DownloadListener() {
    
          @Override
          public void onThrowable(Throwable t) {
             System.err.println("Object download error");
             t.printStackTrace();
          }
    
          @Override
          public void onData(ByteBuffer bodyPart) {
             try {
                fos.write(b);
             } catch (IOException e) {
                System.err.println("Writing data error");
                e.printStackTrace();
             }
		   }
    
          @Override
          public void onCompleted() {
             System.out.println("Object download done");
             fos.flush();
             fos.close();
          }
    
          @Override
          public void onPositionCompleted(int pos) {
          }
       });

###### List objects inside a container

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    Future<ObjectList> f = client.listContainer(url, new ListOptions(),
       new CompletionListener<ObjectInfo>() {
             @Override
             public void onThrowable(Throwable t) {
                System.err.println("Object deletion failure");
                e.printStackTrace();
             }
        
             @Override
             public void onResponse(ObjectInfo obj) throws Exception {
             System.out.println("Object deleted");
       });
            
###### Delete a content

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME", "MY_OBJECT_NAME");
    Future<ObjectInfo> f = client.deleteObject(url, new CompletionListener<ObjectInfo>() {
       @Override
       public void onThrowable(Throwable t) {
             System.err.println("Object deletion failure");
             e.printStackTrace();
       }
       
       @Override
       public void onResponse(ObjectInfo obj) throws Exception {
            System.out.println("Object deleted");
       }
    });

###### Delete a container

An empty container (it should be explicitly empty) could be deleted from your SDS namespace, as follow.

    OioUrl url = OioUrl.url("MY_ACCOUNT", "MY_CONTAINER_NAME");
    Future<Boolean> f = client.deleteContainer(url, new CompletionListener<ContainerInfo>() {
       @Override
       public void onThrowable(Throwable t) {
             System.err.println("Container deletion failure");
             e.printStackTrace();
       }
       
       @Override
       public void onResponse(ContainerInfo obj) throws Exception {
            System.out.println("Container deleted");
       }
    });
