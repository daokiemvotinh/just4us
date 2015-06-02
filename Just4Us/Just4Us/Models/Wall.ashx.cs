using JsonServices;
using JsonServices.Web;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace Just4Us.Models
{
    /// <summary>
    /// Summary description for Wall
    /// </summary>
    public class Wall : JsonHandler
    {

        //public void ProcessRequest(HttpContext context)
        //{
        //    context.Response.ContentType = "text/plain";
        //    context.Response.Write("Hello World");
        //}

        //public bool IsReusable
        //{
        //    get
        //    {
        //        return false;
        //    }
        //}
        public Wall()
        {
            this.service.Name = "JSONWebAPI";
            this.service.Description = "JSON API for android appliation";
            InterfaceConfiguration IConfig = new InterfaceConfiguration("RestAPI", typeof(IServiceAPI), typeof(ServiceAPI));
            this.service.Interfaces.Add(IConfig);
        }
    }
}