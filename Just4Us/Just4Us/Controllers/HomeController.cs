using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace Just4Us.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            return View();
        }

        [HttpPost]
        [ValidateInput(false)]
        public ActionResult Index(FormCollection collect)
        {
            String urlImage = UploadFileToServer();
            ViewBag.urlImage = urlImage;
            return View();
        }

        public string UploadFileToServer()
        {
            string fileNameDK = string.Empty;
            string filePathSave = string.Empty;

            foreach (string inputTagName in Request.Files)
            {
                HttpPostedFileBase file = Request.Files[inputTagName];
                if (file.ContentLength > 0)
                {
                    //OK
                    string dateNow = DateTime.Now.ToString("yyyyMMddhhmmss");
                    fileNameDK = Path.GetFileNameWithoutExtension(file.FileName) + Path.GetExtension(file.FileName);
                    string fileName = Path.GetFileNameWithoutExtension(file.FileName) + "_" + dateNow;
                    string fileExtend = Path.GetExtension(file.FileName);
                    filePathSave = fileName + fileExtend;

                    string Duongdan = Server.MapPath("~/Images/UserUpload");// cai nay lam no hok chay tren 
                    if (!Directory.Exists(Duongdan))
                    {
                        Directory.CreateDirectory(Duongdan);
                    }
                    string filePath = Path.Combine(Duongdan, filePathSave);
                    file.SaveAs(filePath);
                    return filePathSave;
                }
            }
            return null;
        }
    }
}
