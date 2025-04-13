#include <wx/app.h>

#include "mainframe.h"

class CApp : public wxApp
{
public:
    bool OnInit() override
    {
        return (new CMainFrame())->Show();
    }
};

IMPLEMENT_APP(CApp)