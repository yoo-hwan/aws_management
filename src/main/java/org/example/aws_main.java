package org.example;

/*
 * Cloud Computing
 *
 * Dynamic Resource Management Tool
 * using AWS Java SDK Library
 *
 */

import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;

import com.amazonaws.services.ec2.model.*;

import java.util.List;




public class aws_main {

    static AmazonEC2    ec2;

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
                .withRegion("us-east-1")	/* check the region at AWS console */
                .build();
    }

    public static void main(String[] args) throws Exception {

        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;

        while(true)
        {
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
            System.out.println(" 13. delete security group                                  ");
            System.out.println("                                 99. quit                   ");
            System.out.println("------------------------------------------------------------");

            System.out.print("Enter an integer: ");

            if(menu.hasNextInt()){
                number = menu.nextInt();
            }else {
                System.out.println("concentration!");
                break;
            }


            String instance_id = "";

            switch(number) {
                case 1:
                    listInstances();
                    break;

                case 2:
                    availableZones();
                    break;

                case 3:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        startInstance(instance_id);
                    break;

                case 4:
                    availableRegions();
                    break;

                case 5:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
                        stopInstance(instance_id);
                    break;

                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if(id_string.hasNext())
                        ami_id = id_string.nextLine();

                    if(!ami_id.isBlank())
                        createInstance(ami_id);
                    break;

                case 7:
                    System.out.print("Enter instance id: ");
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
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
                    if(id_string.hasNext())
                        instance_id = id_string.nextLine();

                    if(!instance_id.isBlank())
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
                default: System.out.println("concentration!");
            }

        }

    }

    public static void listInstances() {

        System.out.println("Listing instances....");
        boolean done = false;

        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while(!done) {
            DescribeInstancesResult response = ec2.describeInstances(request);

            for(Reservation reservation : response.getReservations()) {
                for(Instance instance : reservation.getInstances()) {
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

            if(response.getNextToken() == null) {
                done = true;
            }
        }
    }

    public static void availableZones()	{

        System.out.println("Available zones....");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();

            AvailabilityZone zone;
            while(iterator.hasNext()) {
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
    startInstance(String instance_id)
    {

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

        for(Region region : regions_response.getRegions()) {
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

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
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

        } catch(Exception e)
        {
            System.out.println("Exception: "+e.toString());
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

        for(Image images :results.getImages()){
            System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    images.getImageId(), images.getName(), images.getOwnerId());
        }
    }

    public static void runCondorStatus() {

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
        // 사용자로부터 입력 받기
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter security group ID : ");
        String securityGroupId = scanner.nextLine();

        // AWS EC2 클라이언트 생성
        AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

        // 보안 그룹 삭제 요청
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
