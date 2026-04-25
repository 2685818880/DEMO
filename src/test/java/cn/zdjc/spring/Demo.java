package cn.zdjc.spring;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.foeris.y.common.json.JsonUtl;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class Demo {
    @Test
    public void demo() {
        String token = "{\"access_token\":\"Mbf9Da2w5IxWIbOYSBC4ofBoxH1F\",\"refresh_token\":\"4dk9MJKXVwi4VFlNyoGKdRvE0gxhXJI3EQRAnSLCpk\",\"id_token\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6ImJPMm0wWktvNUxDa0NybEVnX2lHamJGdVlSYyJ9.eyJzdWIiOiJTRVNBMzQwMjExIiwiYXVkIjoid3Bmd2FyZWhvdXNlLXVhdCIsImp0aSI6InFkMDNOdVkyWjZSNGlRVzhKNjdDd1kiLCJpc3MiOiJodHRwczovL3Bpbmctc3NvLXVhdC5zY2huZWlkZXItZWxlY3RyaWMuY29tIiwiaWF0IjoxNTkxNTk0NjU0LCJleHAiOjE1OTE1OTU1NTR9.NYKXt__tLfBnRnK-MCnxdSuxr6q1Psg-I8_fry3Ety96SSBPan6uE7dIRO166MtCe9ljU5w-F6R57oZThO3C5J4bEkOBGTaUkMf42oq_wfkbE4oqB-11_1tC9CE2FdjsyCgorkZQ18ossDLl5GfZ8ai15jae2Pk0xEYof7bLTE8jaKjpjbi4kIMnlqAuorH_EfMJLDca28mkwALwv3masZeNRv5IFzqbH_tBA7opvGn5cI1G-1twyFhDCYJXwVMLXrcuKXthTEoV-zR1Hqf_rwPjnVl3D6yXf2p1L1QFhI3xFZIdI5ArMHiHWFRNcjbZIW3yv4XTCr3woF_WwBImJhQ\",\"token_type\":\"Bearer\",\"expires_in\":86399}";
        System.out.println(token);
        Map<String, Object> resultMap = JsonUtl.formatMap(token, String.class, Object.class);
        String object = (String)resultMap.get("access_token");
        String object2 = (String)resultMap.get("refresh_token");
        String object3 = (String)resultMap.get("id_token");
        Map<String, Claim> claims = JWT.decode(object3).getClaims();
        System.out.println(claims);
        Claim claim = (Claim)claims.get("sub");
        String asString = claim.asString();
        System.out.println(asString);
        String token2 = "{\"sub\":\"SESA340211\",\"c\":\"CN\",\"physicalDeliveryOfficeName\":\"T2-WuXi-WPF\",\"sAMAccountName\":\"SESA340211\",\"employeeID\":\"SESA340211\",\"given_name\":\"YuFeng MAO\",\"l\":\"Wuxi\",\"title\":\"Senior Method Engineer\",\"name\":\"YuFeng MAO\",\"company\":\"Sch Wuxi Proface Factory\",\"memberOf\":[\"CN=DL CN SCCM Notice,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=DL Global GSC China,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=DL CN GSC China Group,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=DL APA CACC-APAC employee,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL Global Digital Engineering users - 3,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD ARM Creo4Suite,OU=ARM Groups,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD CSE Kollective ECDN Agent 10.6,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD ARM FreeLicense,OU=ARM Groups,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD ARM AutoCAD,OU=ARM Groups,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=DL CN WESOP Eligible,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=DL CN China employee by FiSS,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL CN SAP Users,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL CN GSC All Employees,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD CSE PaloAlto_GlobalProtect_4.0.6_EN_R01,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL CN China EE,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-SSLVPN_Users,OU=VPN,OU=Groups,OU=ConnectivityServices,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-NextgenPDM_PreProd2,OU=Access Management,OU=Groups,DC=gad,DC=schneider-electric,DC=com\",\"CN=CN012-SG-TEAM-WPF-ALL,OU=01-Main,OU=File Server Groups,OU=Groups,OU=CN,OU=Countries,DC=apa,DC=gad,DC=schneider-electric,DC=com\",\"CN=CN512-SG-TEAM-DCC-R,OU=01-Main,OU=File Server Groups,OU=Groups,OU=CN,OU=Countries,DC=apa,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL CN.GSC China,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL APA CN.Territory.Global Supply Chain2,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG DL APA CN All_Employee,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=MSG CN.WPF.Method,OU=DistributionGroups,OU=MSG,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-NextGenPDM_TRN,OU=Access Management,OU=Groups,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD_O365_Teams,OU=Install_profiles,OU=Office_365,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-NextGenPDM_NonPROD,OU=Access Management,OU=Groups,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD_O365_Yammer,OU=Install_profiles,OU=Office_365,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD_O365_Planner,OU=Install_profiles,OU=Office_365,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD_O365_Inst_Access,OU=Install_profiles,OU=Office_365,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\",\"CN=CN012-SG-TEAM-WPF-Method,OU=Wuxi-WPF,OU=File Server Groups,OU=Groups,OU=CN,OU=Countries,DC=apa,DC=gad,DC=schneider-electric,DC=com\",\"CN=CN-SG-APPL-Webfilter,OU=Funtional Groups,OU=Groups,OU=CN,OU=Countries,DC=apa,DC=gad,DC=schneider-electric,DC=com\",\"CN=CN-SG-SSLVPN,OU=Funtional Groups,OU=Groups,OU=CN,OU=Countries,DC=apa,DC=gad,DC=schneider-electric,DC=com\",\"CN=GAD-SU-APPL-CM_SD_O365_Lng_ZH-CN,OU=Install_languages,OU=Office_365,OU=Global,OU=Applications,OU=Deployment,OU=CM,OU=GlobalAdmin,DC=gad,DC=schneider-electric,DC=com\"],\"sn\":\"MAO\",\"email\":\"yufeng.mao@se.com\"}";
        Map<String, Object> resultMap2 = JsonUtl.formatMap(token2, String.class, Object.class);
        Object object4 = resultMap2.get("sub");
        Object object5 = resultMap2.get("given_name");
        System.out.println(object4);
        System.out.println(object5);


    }
}
