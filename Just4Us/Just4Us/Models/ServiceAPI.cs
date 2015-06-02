using System;
using System.Collections.Generic;
using System.Data.Entity.Validation;
using System.Diagnostics;
using System.Linq;
using System.Web;
using System.Web.Script.Serialization;

namespace Just4Us.Models
{
    public class ServiceAPI : IServiceAPI
    {
        Just4UsEntities entity = new Just4UsEntities();
        //public string UploadImage()
        //{
        //    string fileNameDK = string.Empty;
        //    string filePathSave = string.Empty;

        //    foreach (string inputTagName in Request.Files)
        //    {
        //        HttpPostedFileBase file = Request.Files[inputTagName];
        //        if (file.ContentLength > 0)
        //        {
        //            //OK
        //            string dateNow = DateTime.Now.ToString("yyyyMMddhhmmss");
        //            fileNameDK = Path.GetFileNameWithoutExtension(file.FileName) + Path.GetExtension(file.FileName);
        //            string fileName = Path.GetFileNameWithoutExtension(file.FileName) + "_" + dateNow;
        //            string fileExtend = Path.GetExtension(file.FileName);
        //            filePathSave = fileName + fileExtend;

        //            string Duongdan = Server.MapPath("~/Images/UserUpload");// cai nay lam no hok chay tren 
        //            if (!Directory.Exists(Duongdan))
        //            {
        //                Directory.CreateDirectory(Duongdan);
        //            }
        //            string filePath = Path.Combine(Duongdan, filePathSave);
        //            file.SaveAs(filePath);
        //            return filePathSave;
        //        }
        //    }
        //    return null;
        //}
        public string LoadWallData(int userID)
        {
            var lstEntity = entity.tbl_J4U_Wall.Where(s => s.userID.Value == userID).OrderByDescending(t=>t.dateUpdate).ToList();
            JavaScriptSerializer js = new JavaScriptSerializer();
            string json = js.Serialize(lstEntity);
            return json;
        }
        public string UploadImage()
        {
            return null;
        }

        public int CreateNewStatus(int userID, string status, string urlImage)
        {
            try
            {
                tbl_J4U_Wall item = new tbl_J4U_Wall();
                item.userID = userID;
                item.status = status;
                item.url = urlImage;
                item.dateUpdate = DateTime.Now;
                entity.tbl_J4U_Wall.Add(item);
                return entity.SaveChanges();
            }
            catch (DbEntityValidationException dbEx)
            {
                foreach (var validationErrors in dbEx.EntityValidationErrors)
                {
                    foreach (var validationError in validationErrors.ValidationErrors)
                    {
                        Trace.TraceInformation("Property: {0} Error: {1}", validationError.PropertyName, validationError.ErrorMessage);
                    }
                }
            }
            return 0;
        }
    }
}