package org.example;

/*
 * Cloud Computing
 *
 * Dynamic Resource Management Tool
 * using AWS Java SDK Library
 *
 */

import java.util.*;
import java.io.*;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ec2.model.*;
import com.jcraft.jsch.*;

public class aws_main {

    static AmazonEC2 ec2;
    private static String keyname = "C:/Users/Administrator/Downloads/project-key.pem";
//    private static String publicDNS = "ec2-54-83-89-251.compute-1.amazonaws.com";
    private static String publicDNS = "54.237.67.44";

    private static void init() throws Exception {

        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion("us-east-1")    /* check the region at AWS console */
                .build();
    }

    public static void main(String[] args) throws Exception {

        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;

        while (true) {
            System.out.println("                                                            ");
            System.out.println("                                                            ");
            System.out.println("------------------------------------------------------------");
            System.out.println("           Amazon AWS Control Panel using SDK               ");
            System.out.println("------------------------------------------------------------");
            System.out.println("  1. list instance                2. available zones        ");
            System.out.println("  3. start instance               4. available regions      ");
            System.out.println("  5. stop instance                6. create instance        ");
            System.out.println("  7. reboot instance              8. list images            ");
            System.out.println("  9. condor_status               10. terminate instance     ");
            System.out.println(" 11. list security group         12. create security group  ");
            System.out.println(" 13. delete security group       99. quit                   ");
            System.out.println("------------------------------------------------------------");

            System.out.print("Enter an integer: ");

            if (menu.hasNextInt()) {
                number = menu.nextInt();
            } else {
                System.out.println("concentration!");
                break;
            }


            String instance_id = "";

            switch (number) {
                case 1:
                    listInstances();
                    break;

                case 2:
                    availableZones();
                    break;

                case 3:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if (!instance_id.isBlank())
                        startInstance(instance_id);
                    break;

                case 4:
                    availableRegions();
                    break;

                case 5:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if (!instance_id.isBlank())
                        stopInstance(instance_id);
                    break;

                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if (id_string.hasNext())
                        ami_id = id_string.nextLine();

                    if (!ami_id.isBlank())
                        createInstance(ami_id);
                    break;

                case 7:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if (!instance_id.isBlank())
                        rebootInstance(instance_id);
                    break;

                case 8:
                    listImages();
                    break;

                case 9:
                    runCondorStatus();
                    break;

                case 10:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if (!instance_id.isBlank())
                        terminateInstance(instance_id);
                    break;

                case 11:
                    listSecurityGroups();
                    break;

                case 12:
                    createSecurityGroup();
                    break;

                case 13:
                    deleteSecurityGroup();
                    break;


                case 99:
                    System.out.println("bye!");
                    menu.close();
                    id_string.close();
                    return;
                default:
                    System.out.println("concentration!");
            }

        }

    }

    public static void listInstances() {

        System.out.println("Listing instances....");
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while (!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for (Reservation reservation : response.getReservations()) {
                for (Instance instance : reservation.getInstances()) {
                    System.out.printf(
                            "[id] %s, " +
                                    "[AMI] %s, " +
                                    "[type] %s, " +
                                    "[state] %10s, " +
                                    "[monitoring state] %s",
                            instance.getInstanceId(),
                            instance.getImageId(),
                            instance.getInstanceType(),
                            instance.getState().getName(),
                            instance.getMonitoring().getState());
                }
                System.out.println();
            }

            request.setNextToken(response.getNextToken());

            if (response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public static void availableZones() {

        System.out.println("Available zones....");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            Iterator<AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

            AvailabilityZone zone;
            while (iterator.hasNext()) {
                zone = iterator.next();
                System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
            }
            System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
                    " Availability Zones.");

        } catch (AmazonServiceException ase) {
            System.out.println("Caught Exception: " + ase.getMessage());
            System.out.println("Reponse Status Code: " + ase.getStatusCode());
            System.out.println("Error Code: " + ase.getErrorCode());
            System.out.println("Request ID: " + ase.getRequestId());
        }

    }

    public static void
    startInstance(String instance_id) {

        System.out.printf("Starting .... %s\n", instance_id);
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StartInstancesRequest> dry_request =
                () -> {
                    StartInstancesRequest request = new StartInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        StartInstancesRequest request = new StartInstancesRequest()
                .withInstanceIds(instance_id);

        ec2.startInstances(request);

        System.out.printf("Successfully started instance %s", instance_id);
    }


    public static void availableRegions() {

        System.out.println("Available regions ....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeRegionsResult regions_response = ec2.describeRegions();

        for (Region region : regions_response.getRegions()) {
            System.out.printf(
                    "[region] %15s, " +
                            "[endpoint] %s\n",
                    region.getRegionName(),
                    region.getEndpoint());
        }
    }

    public static void stopInstance(String instance_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<StopInstancesRequest> dry_request =
                () -> {
                    StopInstancesRequest request = new StopInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        try {
            StopInstancesRequest request = new StopInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.stopInstances(request);
            System.out.printf("Successfully stop instance %s\n", instance_id);

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }

    }

    public static void createInstance(String ami_id) {
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        RunInstancesRequest run_request = new RunInstancesRequest()
                .withImageId(ami_id)
                .withInstanceType(InstanceType.T2Micro)
                .withMaxCount(1)
                .withMinCount(1);

        RunInstancesResult run_response = ec2.runInstances(run_request);

        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

        System.out.printf(
                "Successfully started EC2 instance %s based on AMI %s",
                reservation_id, ami_id);

    }

    public static void rebootInstance(String instance_id) {

        System.out.printf("Rebooting .... %s\n", instance_id);

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        try {
            RebootInstancesRequest request = new RebootInstancesRequest()
                    .withInstanceIds(instance_id);

            RebootInstancesResult response = ec2.rebootInstances(request);

            System.out.printf(
                    "Successfully rebooted instance %s", instance_id);

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }


    }

    public static void listImages() {
        System.out.println("Listing images....");

        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DescribeImagesRequest request = new DescribeImagesRequest();
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

        request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-slave"));
        request.setRequestCredentialsProvider(credentialsProvider);

        DescribeImagesResult results = ec2.describeImages(request);

        for (Image images : results.getImages()) {
            System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    images.getImageId(), images.getName(), images.getOwnerId());
        }
    }

    public static void runCondorStatus() {
        try {
            JSch jsch = new JSch();

            String user = "ec2-user";
            String host = publicDNS;
            int port = 22;
            String privateKey = keyname;

            jsch.addIdentity(privateKey);

            Session session = jsch.getSession(user, host, port);

            session.setConfig("StrictHostKeyChecking", "no");
            session.setConfig("GSSAPIAuthentication", "no");
            session.setServerAliveInterval(120 * 1000);
            session.setServerAliveCountMax(1000);
            session.setConfig("TCPKeepAlive", "yes");

            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            String command = "sudo su -c 'condor_status'";
            channel.setCommand(command);

            channel.setInputStream(null);
            channel.setOutputStream(System.out);
            channel.setErrStream(System.err);

            channel.connect();

            while (!channel.isEOF()) {
                Thread.sleep(1000);
            }

            channel.disconnect();

            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void terminateInstance(String instance_id){
        final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DryRunSupportedRequest<TerminateInstancesRequest> dryRequest =
                () -> {
                    TerminateInstancesRequest request = new TerminateInstancesRequest()
                            .withInstanceIds(instance_id);

                    return request.getDryRunRequest();
                };

        try {
            TerminateInstancesRequest request = new TerminateInstancesRequest()
                    .withInstanceIds(instance_id);

            ec2.terminateInstances(request);
            System.out.printf("Successfully terminated instance %s\n", instance_id);

        } catch (Exception e) {
            System.out.println("Exception: " + e.toString());
        }
    }

    public static void listSecurityGroups() {
        AmazonEC2 ec2 = AmazonEC2Client.builder()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .build();

        DescribeSecurityGroupsRequest describeSecurityGroupsRequest = new DescribeSecurityGroupsRequest();
        DescribeSecurityGroupsResult describeSecurityGroupsResult = ec2.describeSecurityGroups(describeSecurityGroupsRequest);

        System.out.println("Security Groups:");
        for (SecurityGroup securityGroup : describeSecurityGroupsResult.getSecurityGroups()) {
            System.out.println("ID: " + securityGroup.getGroupId());
            System.out.println("Name: " + securityGroup.getGroupName());
            System.out.println("Description: " + securityGroup.getDescription());
            System.out.println("VPC ID: " + securityGroup.getVpcId());
            System.out.println("Inbound Rules:");
            for (IpPermission ipPermission : securityGroup.getIpPermissions()) {
                System.out.println("  Protocol: " + ipPermission.getIpProtocol());
                System.out.println("  Port Range: " + ipPermission.getFromPort() + " - " + ipPermission.getToPort());
                System.out.println("  Source: " + ipPermission.getIpRanges());
            }
            System.out.println("Outbound Rules:");
            for (IpPermission ipPermission : securityGroup.getIpPermissionsEgress()) {
                System.out.println("  Protocol: " + ipPermission.getIpProtocol());
                System.out.println("  Port Range: " + ipPermission.getFromPort() + " - " + ipPermission.getToPort());
                System.out.println("  Destination: " + ipPermission.getIpRanges());
            }
            System.out.println("----------");
            System.out.println();
        }
    }


    public static void createSecurityGroup(){
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter security group name : ");
        String securityGroupName = scanner.nextLine();

        System.out.print("Enter security group description : ");
        String description = scanner.nextLine();

        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest()
                .withGroupName(securityGroupName)
                .withDescription(description);

        CreateSecurityGroupResult createSecurityGroupResult = ec2.createSecurityGroup(createSecurityGroupRequest);

        String securityGroupId = createSecurityGroupResult.getGroupId();
        System.out.println("Security Group ID: " + securityGroupId);

        authorizeIngress(ec2, securityGroupId, "tcp", 22, 22, "0.0.0.0/0");
    }
    private static void authorizeIngress(AmazonEC2 ec2, String securityGroupId,
                                         String protocol, int fromPort, int toPort, String cidrIp) {

        IpPermission ipPermission = new IpPermission()
                .withIpProtocol(protocol)
                .withFromPort(fromPort)
                .withToPort(toPort)
                .withIpRanges(cidrIp);

        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
                new AuthorizeSecurityGroupIngressRequest()
                        .withGroupId(securityGroupId)
                        .withIpPermissions(ipPermission);

        ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
    }

    public static void deleteSecurityGroup() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter security group ID : ");
        String securityGroupId = scanner.nextLine();

        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest()
                .withGroupId(securityGroupId);

        try {
            ec2.deleteSecurityGroup(deleteSecurityGroupRequest);
            System.out.println("Security Group deleted successfully.");

        } catch (Exception e) {
            System.out.println("Error deleting security group: " + e.getMessage());
        }
    }
}
