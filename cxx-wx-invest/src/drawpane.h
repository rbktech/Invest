#pragma once

#include "wx/sizer.h"
#include "wx/wx.h"

class CDrawPane : public wxPanel
{
private:
    void paintEvent(wxPaintEvent& evt);
    void paintNow();

    wxPointList* mListAssetValue;

    void render(wxDC& dc);

    // some useful events
    /*
     void mouseMoved(wxMouseEvent& event);
     void mouseDown(wxMouseEvent& event);
     void mouseWheelMoved(wxMouseEvent& event);
     void mouseReleased(wxMouseEvent& event);
     void rightClick(wxMouseEvent& event);
     void mouseLeftWindow(wxMouseEvent& event);
     void keyPressed(wxKeyEvent& event);
     void keyReleased(wxKeyEvent& event);
     */

    DECLARE_EVENT_TABLE()

public:
    CDrawPane(wxFrame* parent);
    ~CDrawPane() override;

    void paintAssetPrice(const wxPointList& listAssetValue);

    void setListAssetValue(wxPointList* listAssetValue);
};