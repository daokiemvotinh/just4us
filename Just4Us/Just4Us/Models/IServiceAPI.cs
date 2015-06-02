using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace Just4Us.Models
{
    public interface IServiceAPI
    {
        string UploadImage();
        int CreateNewStatus(int userID, string status, string urlImage);
        string LoadWallData(int userID);
    }
}